package com.happyclaw.hikinghappy.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class SyncRecord(
    val altitude: Double,
    val speed: Double,
    val type: String,
    val location: String?,
    val timestamp: Long
)

@Serializable
data class SyncRequest(
    val records: List<SyncRecord>
)

@Serializable
data class SyncResponse(
    val status: String,
    val upserted: Int
)

@Serializable
data class RestoreResponse(
    val records: List<SyncRecord>,
    val total: Int
)

data class SyncProgress(
    val phase: SyncPhase,
    val percentage: Int,
    val message: String,
    val recordCount: Int = 0
)

enum class SyncPhase {
    PREPARING,
    IN_PROGRESS,
    COMPLETE,
    ERROR
}

@Singleton
class SyncService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityRepository: com.happyclaw.hikinghappy.data.repository.ActivityRepository
) {
    private val deviceKey: String
    private val baseUrl = "https://hiking-happy.api.workers.dev"

    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; encodeDefaults = false })
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
    }

    init {
        deviceKey = getOrCreateDeviceKey()
    }

    private fun getOrCreateDeviceKey(): String {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs: SharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        var key = prefs.getString("device_key", null)
        if (key == null) {
            key = UUID.randomUUID().toString()
            prefs.edit().putString("device_key", key).apply()
        }
        return key
    }

    fun backup(): Flow<SyncProgress> = flow {
        emit(SyncProgress(SyncPhase.PREPARING, 0, "Preparing data..."))

        withContext(Dispatchers.IO) {
            try {
                val totalCount = activityRepository.getRecordCount()
                if (totalCount == 0) {
                    emit(SyncProgress(SyncPhase.COMPLETE, 100, "No data to backup.", 0))
                    return@withContext
                }

                val pageSize = 5000
                var offset = 0
                var syncedTotal = 0

                while (offset < totalCount) {
                    val records = activityRepository.getRecordsPaginated(pageSize, offset)
                    if (records.isEmpty()) break

                    val syncRecords = records.map {
                        SyncRecord(
                            altitude = it.altitude,
                            speed = it.speed,
                            type = it.type.name,
                            location = it.location,
                            timestamp = it.timestamp
                        )
                    }

                    // Retry up to 3 times with exponential backoff
                    var success = false
                    var lastError: String? = null
                    for (attempt in 0 until 3) {
                        try {
                            val response: HttpResponse = httpClient.post("$baseUrl/api/sync") {
                                header("X-Device-Key", deviceKey)
                                setBody(SyncRequest(syncRecords))
                            }
                            if (response.status.isSuccess()) {
                                val syncResponse = response.body<SyncResponse>()
                                syncedTotal += syncResponse.upserted
                                success = true
                                break
                            } else {
                                lastError = "Server error: ${response.status}"
                            }
                        } catch (e: Exception) {
                            lastError = e.message ?: "Unknown error"
                        }
                        if (attempt < 2) {
                            kotlinx.coroutines.delay(1000L * (1L shl attempt))
                        }
                    }

                    if (!success) {
                        emit(SyncProgress(
                            SyncPhase.ERROR, (offset * 100 / totalCount),
                            "Could not upload data. Please check your connection and try again.", syncedTotal
                        ))
                        return@withContext
                    }

                    offset += pageSize
                    val percentage = if (totalCount > 0) (offset * 100 / totalCount).coerceAtMost(99) else 0
                    emit(SyncProgress(
                        SyncPhase.IN_PROGRESS, percentage,
                        "Uploading... $percentage%", syncedTotal
                    ))
                }

                emit(SyncProgress(SyncPhase.COMPLETE, 100, "Backup complete! $syncedTotal records synced.", syncedTotal))
            } catch (e: Exception) {
                emit(SyncProgress(SyncPhase.ERROR, 0, "Backup failed. Please check your connection and try again.", 0))
            }
        }
    }

    fun restore(): Flow<SyncProgress> = flow {
        emit(SyncProgress(SyncPhase.PREPARING, 0, "Preparing to restore..."))

        withContext(Dispatchers.IO) {
            try {
                var totalFetched = 0
                var hasMore = true
                var offset = 0
                val pageSize = 5000

                while (hasMore) {
                    var success = false
                    var lastError: String? = null
                    var responseRecords: List<SyncRecord> = emptyList()
                    var serverTotal = 0

                    for (attempt in 0 until 3) {
                        try {
                            val response: HttpResponse = httpClient.get("$baseUrl/api/restore") {
                                header("X-Device-Key", deviceKey)
                                header("X-Offset", offset.toString())
                                header("X-Limit", pageSize.toString())
                            }
                            if (response.status.isSuccess()) {
                                val restoreResponse = response.body<RestoreResponse>()
                                responseRecords = restoreResponse.records
                                serverTotal = restoreResponse.total
                                success = true
                                break
                            } else {
                                lastError = "Server error: ${response.status}"
                            }
                        } catch (e: Exception) {
                            lastError = e.message ?: "Unknown error"
                        }
                        if (attempt < 2) {
                            kotlinx.coroutines.delay(1000L * (1L shl attempt))
                        }
                    }

                    if (!success) {
                        emit(SyncProgress(
                            SyncPhase.ERROR, (if (serverTotal > 0) offset * 100 / serverTotal else 0),
                            "Could not download data. Please check your connection and try again.", totalFetched
                        ))
                        return@withContext
                    }

                    if (responseRecords.isEmpty()) {
                        hasMore = false
                        break
                    }

                    // Convert and merge into Room (INSERT OR IGNORE by timestamp)
                    val roomRecords = responseRecords.map {
                        ActivityRecord(
                            altitude = it.altitude,
                            speed = it.speed,
                            type = try {
                                ActivityType.valueOf(it.type)
                            } catch (_: Exception) {
                                ActivityType.HIKING
                            },
                            location = it.location,
                            timestamp = it.timestamp
                        )
                    }
                    activityRepository.insertOrIgnore(roomRecords)
                    totalFetched += responseRecords.size
                    offset += pageSize

                    val percentage = if (serverTotal > 0) (offset * 100 / serverTotal).coerceAtMost(99) else 0
                    emit(SyncProgress(
                        SyncPhase.IN_PROGRESS, percentage,
                        "Downloading... $percentage%", totalFetched
                    ))
                }

                val finalCount = activityRepository.getRecordCount()
                emit(SyncProgress(SyncPhase.COMPLETE, 100, "Restore complete! $finalCount local records.", finalCount))
            } catch (e: Exception) {
                emit(SyncProgress(SyncPhase.ERROR, 0, "Restore failed. Please check your connection and try again.", 0))
            }
        }
    }
}
