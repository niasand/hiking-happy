package com.happyclaw.hikinghappy.ui.screens.instruments

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.data.local.entity.TrackPoint
import com.happyclaw.hikinghappy.data.repository.TrackRepository
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import com.happyclaw.hikinghappy.domain.model.GpsSignalState
import com.happyclaw.hikinghappy.domain.model.InstrumentState
import com.happyclaw.hikinghappy.service.LocationUpdate
import com.happyclaw.hikinghappy.service.LocationSensorService
import com.happyclaw.hikinghappy.service.RecordingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InstrumentsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val locationSensorService: LocationSensorService,
    private val trackRepository: TrackRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(InstrumentState())
    val state: StateFlow<InstrumentState> = _state.asStateFlow()

    // Speed smoothing buffer (3-sample moving average)
    private val speedBuffer = mutableListOf<Double>()

    // Recording duration timer
    private var recordingTimerJob: Job? = null
    private var recordingStartTime: Long = 0L

    // Track points for the active session — exposed to UI
    val trackPoints: StateFlow<List<TrackPoint>> = trackRepository.getAllSessions()
        .flatMapLatest { sessions ->
            val activeSession = sessions.firstOrNull { it.endTime == null }
            if (activeSession != null) {
                trackRepository.getPointsForSession(activeSession.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe user preferences (activity type, location, units)
        viewModelScope.launch {
            combine(
                preferencesRepository.activityType,
                preferencesRepository.location,
                preferencesRepository.altitudeUnitIndex,
                preferencesRepository.speedUnitIndex
            ) { type, location, altIdx, spdIdx ->
                arrayOf(type, location, altIdx, spdIdx)
            }.collect { (type, location, altIdx, spdIdx) ->
                _state.value = _state.value.copy(
                    activityType = type as ActivityType,
                    location = location as String,
                    altitudeUnitIndex = altIdx as Int,
                    speedUnitIndex = spdIdx as Int,
                    altitudeUnitLabel = if (altIdx == 0) "m" else "ft",
                    speedUnitLabel = if (spdIdx == 0) "km/h" else "mph"
                )
            }
        }

        // Observe GPS location updates for UI display only
        viewModelScope.launch {
            locationSensorService.locationUpdates
                .collect { update ->
                    if (update != null) {
                        processLocationUpdate(update)
                    } else {
                        _state.value = _state.value.copy(
                            gpsState = GpsSignalState.LOST,
                            displaySpeed = 0.0,
                            isGpsAcquiring = true
                        )
                    }
                }
        }

        // Set initial barometer state
        _state.value = _state.value.copy(
            hasBarometer = locationSensorService.hasBarometer
        )

        // Sync recording state from static flag
        _state.value = _state.value.copy(isRecording = RecordingService.isRecording)
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

        // 3-sample moving average for display
        speedBuffer.add(filteredSpeed)
        if (speedBuffer.size > 3) speedBuffer.removeAt(0)
        val smoothedSpeed = speedBuffer.average()

        // Final zero-out: if smoothed speed is below threshold, show exactly 0
        val displaySpeed = if (smoothedSpeed < 0.5) 0.0 else smoothedSpeed

        // Update UI state only — recording is handled by RecordingService
        _state.value = _state.value.copy(
            altitude = update.altitude,
            speed = filteredSpeed,
            displaySpeed = displaySpeed,
            gpsState = gpsState,
            hasBarometer = update.hasBarometer,
            isGpsAcquiring = false,
            latitude = update.latitude,
            longitude = update.longitude
        )
    }

    fun startRecording() {
        val state = _state.value
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_START_RECORDING
            putExtra(RecordingService.EXTRA_ACTIVITY_TYPE, state.activityType.name)
            putExtra(RecordingService.EXTRA_LOCATION, state.location)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        recordingStartTime = System.currentTimeMillis()
        _state.value = _state.value.copy(
            isRecording = true,
            recordingDurationSec = 0L
        )
        startRecordingTimer()
    }

    fun stopRecording() {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP_RECORDING
        }
        context.startService(intent)

        _state.value = _state.value.copy(isRecording = false)
        recordingTimerJob?.cancel()
        recordingTimerJob = null
    }

    private fun startRecordingTimer() {
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val elapsed = (System.currentTimeMillis() - recordingStartTime) / 1000
                _state.value = _state.value.copy(recordingDurationSec = elapsed)
            }
        }
    }

    fun setActivityType(type: ActivityType) {
        viewModelScope.launch {
            preferencesRepository.setActivityType(type)
        }
    }

    fun setLocation(location: String) {
        viewModelScope.launch {
            preferencesRepository.setLocation(location)
        }
    }

    /** Request a fresh GPS fix and bump counter to trigger map re-center */
    fun refreshLocation() {
        locationSensorService.requestSingleLocation()
        _state.value = _state.value.copy(locationRefreshCounter = _state.value.locationRefreshCounter + 1)
    }
}
