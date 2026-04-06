package com.happyclaw.hikinghappy.ui.screens.instruments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyclaw.hikinghappy.data.local.entity.ActivityRecord
import com.happyclaw.hikinghappy.data.local.entity.ActivityType
import com.happyclaw.hikinghappy.data.repository.ActivityRepository
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import com.happyclaw.hikinghappy.domain.model.GpsSignalState
import com.happyclaw.hikinghappy.domain.model.InstrumentState
import com.happyclaw.hikinghappy.service.LocationUpdate
import com.happyclaw.hikinghappy.service.LocationSensorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@HiltViewModel
class InstrumentsViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val locationSensorService: LocationSensorService
) : ViewModel() {

    private val _state = MutableStateFlow(InstrumentState())
    val state: StateFlow<InstrumentState> = _state.asStateFlow()

    // Speed smoothing buffer (3-sample moving average)
    private val speedBuffer = mutableListOf<Double>()

    // Cache for preferences -- avoids reading DataStore on every GPS tick
    private var cachedActivityType: ActivityType = ActivityType.HIKING
    private var cachedLocation: String = ""

    init {
        // Observe user preferences (activity type, location, units)
        viewModelScope.launch {
            combine(
                preferencesRepository.activityType,
                preferencesRepository.location,
                preferencesRepository.altitudeUnitIndex,
                preferencesRepository.speedUnitIndex
            ) { type, location, altIdx, spdIdx ->
                Quadruple(type, location, altIdx, spdIdx)
            }.collect { (type, location, altIdx, spdIdx) ->
                cachedActivityType = type
                cachedLocation = location
                _state.value = _state.value.copy(
                    activityType = type,
                    location = location,
                    altitudeUnitIndex = altIdx,
                    speedUnitIndex = spdIdx,
                    altitudeUnitLabel = if (altIdx == 0) "m" else "ft",
                    speedUnitLabel = if (spdIdx == 0) "km/h" else "mph"
                )
            }
        }

        // Observe GPS location updates from service
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

        // 3-sample moving average for display
        speedBuffer.add(filteredSpeed)
        if (speedBuffer.size > 3) speedBuffer.removeAt(0)
        val smoothedSpeed = speedBuffer.average()

        _state.value = _state.value.copy(
            altitude = update.altitude,
            speed = filteredSpeed,
            displaySpeed = smoothedSpeed,
            gpsState = gpsState,
            hasBarometer = update.hasBarometer,
            isGpsAcquiring = false
        )

        // Auto-record to Room only when GPS signal is ACTIVE or WEAK
        // Skip recording when POOR (accuracy > 50m) to avoid data pollution
        if (gpsState == GpsSignalState.ACTIVE || gpsState == GpsSignalState.WEAK) {
            viewModelScope.launch {
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
}
