package com.happyclaw.hikinghappy.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint
import com.happyclaw.hikinghappy.data.repository.ActivityRepository
import com.happyclaw.hikinghappy.data.repository.TrackRepository
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import com.happyclaw.hikinghappy.domain.model.GpsSignalState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : Service() {

    companion object {
        const val ACTION_START_RECORDING = "com.happyclaw.hikinghappy.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.happyclaw.hikinghappy.STOP_RECORDING"
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
        const val EXTRA_LOCATION = "location"

        var isRecording = false
            private set
    }

    @Inject lateinit var locationSensorService: LocationSensorService
    @Inject lateinit var activityRepository: ActivityRepository
    @Inject lateinit var trackRepository: TrackRepository
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    private var notificationJob: Job? = null
    private var flushJob: Job? = null

    // Current state for notification updates
    private var currentAltitude: Double = Double.NaN
    private var currentSpeed: Double = 0.0
    private var startTime: Long = 0L

    // Cached preferences
    private var cachedActivityType = ActivityType.HIKING
    private var cachedLocation = ""

    // Track recording state
    private var currentSessionId: Long = -1L
    private val trackPointBatch = CopyOnWriteArrayList<TrackPoint>()

    override fun onCreate() {
        super.onCreate()
        RecordingNotification.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                if (!isRecording) {
                    val activityType = intent.getStringExtra(EXTRA_ACTIVITY_TYPE)?.let {
                        try { ActivityType.valueOf(it) } catch (_: Exception) { ActivityType.HIKING }
                    } ?: ActivityType.HIKING
                    val location = intent.getStringExtra(EXTRA_LOCATION) ?: ""
                    startRecording(activityType, location)
                }
            }
            ACTION_STOP_RECORDING -> {
                if (isRecording) {
                    stopRecording()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRecording(activityType: ActivityType, location: String) {
        isRecording = true
        startTime = System.currentTimeMillis()
        cachedActivityType = activityType
        cachedLocation = location

        // Start foreground with initial notification
        val notification = RecordingNotification.build(
            context = this,
            altitude = Double.NaN,
            speed = 0.0,
            durationSec = 0L
        )
        startForeground(RecordingNotification.NOTIFICATION_ID, notification)

        // Create track session
        serviceScope.launch {
            currentSessionId = trackRepository.startSession(
                activityType = activityType,
                location = location.takeIf { it.isNotBlank() }
            )
        }

        // Observe preferences for live updates
        serviceScope.launch {
            combine(
                preferencesRepository.activityType,
                preferencesRepository.location
            ) { type, loc -> type to loc }
                .collect { (type, loc) ->
                    cachedActivityType = type
                    cachedLocation = loc
                }
        }

        // Start GPS recording
        locationJob = serviceScope.launch {
            locationSensorService.locationUpdates.collect { update ->
                if (update != null && isRecording) {
                    processLocationUpdate(update)
                }
            }
        }

        // Update notification every 5 seconds
        notificationJob = serviceScope.launch {
            while (true) {
                updateNotification()
                delay(5000L)
            }
        }

        // Flush track point batch every 5 seconds
        flushJob = serviceScope.launch {
            while (true) {
                delay(5000L)
                flushTrackPointBatch()
            }
        }
    }

    private fun stopRecording() {
        isRecording = false

        // Final flush
        serviceScope.launch { flushTrackPointBatch() }

        // Finalize session
        serviceScope.launch {
            if (currentSessionId > 0) {
                trackRepository.finalizeSession(currentSessionId)
                currentSessionId = -1L
            }
        }

        // Cancel coroutines
        locationJob?.cancel()
        notificationJob?.cancel()
        flushJob?.cancel()
        trackPointBatch.clear()

        // Stop foreground and service
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun processLocationUpdate(update: LocationUpdate) {
        val gpsState = when {
            update.accuracy == null -> GpsSignalState.LOST
            update.accuracy > 50f -> GpsSignalState.POOR
            update.accuracy > 20f -> GpsSignalState.WEAK
            else -> GpsSignalState.ACTIVE
        }

        // Speed dead zone filter: < 0.5 m/s (1.8 km/h) -> 0
        val filteredSpeed = if (update.speed < 0.5f) 0.0 else update.speed.toDouble()

        currentAltitude = update.altitude
        currentSpeed = filteredSpeed

        // Record only when GPS signal is ACTIVE or WEAK
        if (gpsState == GpsSignalState.ACTIVE || gpsState == GpsSignalState.WEAK) {
            serviceScope.launch(Dispatchers.IO) {
                val loc = cachedLocation.ifBlank { null }
                activityRepository.insertRecord(
                    ActivityRecord(
                        altitude = update.altitude,
                        speed = filteredSpeed,
                        type = cachedActivityType,
                        location = loc,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            // Add to track point batch
            if (currentSessionId > 0) {
                trackPointBatch.add(
                    TrackPoint(
                        sessionId = currentSessionId,
                        latitude = update.latitude,
                        longitude = update.longitude,
                        altitude = update.altitude,
                        speed = update.speed,
                        accuracy = update.accuracy,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    private suspend fun flushTrackPointBatch() {
        if (currentSessionId <= 0 || trackPointBatch.isEmpty()) return

        val toFlush = trackPointBatch.toList()
        trackPointBatch.clear()

        try {
            trackRepository.addTrackPoints(toFlush)
        } catch (e: Exception) {
            trackPointBatch.addAll(0, toFlush)
        }
    }

    private fun updateNotification() {
        if (!isRecording) return
        val durationSec = (System.currentTimeMillis() - startTime) / 1000
        val notification = RecordingNotification.build(
            context = this,
            altitude = currentAltitude,
            speed = currentSpeed,
            durationSec = durationSec
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(RecordingNotification.NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        if (isRecording) {
            stopRecording()
        }
        super.onDestroy()
    }
}
