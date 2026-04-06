package com.happyclaw.hikinghappy.domain.model

import com.happyclaw.hikinghappy.data.local.entity.ActivityType

data class SensorReading(
    val altitude: Double,       // meters, SI
    val speed: Double,          // m/s, SI (raw, before dead zone filter)
    val gpsAccuracy: Float?,    // meters, null if no fix
    val hasBarometer: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class InstrumentState(
    val altitude: Double = Double.NaN,
    val speed: Double = 0.0,
    val displaySpeed: Double = 0.0,  // smoothed for display
    val gpsState: GpsSignalState = GpsSignalState.LOST,
    val hasBarometer: Boolean = false,
    val isGpsAcquiring: Boolean = true,
    val activityType: ActivityType = ActivityType.HIKING,
    val location: String = "",
    val altitudeUnitLabel: String = "m",
    val speedUnitLabel: String = "km/h",
    val altitudeUnitIndex: Int = 0,
    val speedUnitIndex: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationRefreshCounter: Int = 0
) {
    fun getAltitudeUnit(): AltitudeUnit = if (altitudeUnitIndex == 0) AltitudeUnit.METERS else AltitudeUnit.FEET
    fun getSpeedUnit(): SpeedUnit = if (speedUnitIndex == 0) SpeedUnit.KMH else SpeedUnit.MPH
}

data class AltitudeUnit(val label: String, val conversion: (Double) -> Double) {
    companion object {
        val METERS = AltitudeUnit("m") { it }
        val FEET = AltitudeUnit("ft") { it * 3.28084 }
    }
}

data class SpeedUnit(val label: String, val conversion: (Double) -> Double) {
    companion object {
        val KMH = SpeedUnit("km/h") { it * 3.6 }
        val MPH = SpeedUnit("mph") { it * 2.23694 }
    }
}
