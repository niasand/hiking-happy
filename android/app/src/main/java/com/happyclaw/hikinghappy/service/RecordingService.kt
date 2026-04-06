package com.happyclaw.hikinghappy.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.data.repository.ActivityRepository
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
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : Service() {

    @Inject lateinit var locationSensorService: LocationSensorService
    @Inject lateinit var activityRepository: ActivityRepository
    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationJob: Job? = null
    private var notificationJob: Job? = null

    // Current state for notification updates
    private var currentAltitude: Double = Double.NaN
    private var currentSpeed: Double = 0.0
    private var startTime: Long = 0L

    // Cached preferences (avoid DataStore reads on every GPS tick)
    private var cachedActivityType = com.happyclaw.hikinghappy.data.local.entity.ActivityType.HIKING
    private var cachedLocation = ""

    override fun onCreate() {
        super.onCreate()
        startTime = System.currentTimeMillis()
        RecordingNotification.createChannel(this)

        // Observe preferences
        serviceScope.launch {
            combine(
                preferencesRepository.activityType,
                preferencesRepository.location
            ) { type, location ->
                type to location
            }.collect { (type, location) ->
                cachedActivityType = type
                cachedLocation = location
            }
        }

        // Start GPS recording
        locationJob = serviceScope.launch {
            locationSensorService.locationUpdates.collect { update ->
                if (update != null) {
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create initial notification and start foreground
        val notification = RecordingNotification.build(
            context = this,
            altitude = Double.NaN,
            speed = 0.0,
            durationSec = 0L
        )
        startForeground(RecordingNotification.NOTIFICATION_ID, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationJob?.cancel()
        notificationJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun processLocationUpdate(update: LocationUpdate) {
        val gpsState = when {
            update.accuracy == null -> GpsSignalState.LOST
            update.accuracy > 50f -> GpsSignalState.POOR
            update.accuracy > 20f -> GpsSignalState.WEAK
            else -> GpsSignalState.ACTIVE
        }

        // Speed dead zone filter: < 0.278 m/s (1 km/h) -> 0
        val filteredSpeed = if (update.speed < 0.278f) 0.0 else update.speed.toDouble()

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
        }
    }

    private fun updateNotification() {
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
}
