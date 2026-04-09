package com.happyclaw.hikinghappy.service

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class LocationSensorService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val movementDetector: MovementDetector
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    val hasBarometer: Boolean = pressureSensor != null

    // Sea-level pressure reference for barometric altitude calculation
    private var seaLevelPressure: Float = SensorManager.PRESSURE_STANDARD_ATMOSPHERE
    private var isSeaLevelCalibrated = false
    private var latestPressure: Float = SensorManager.PRESSURE_STANDARD_ATMOSPHERE

    // Last two GPS fixes for interpolation
    @Volatile private var prevGpsUpdate: GpsFix? = null
    @Volatile private var lastGpsUpdate: GpsFix? = null
    @Volatile private var interpolationActive = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val locationUpdates: StateFlow<LocationUpdate?> = callbackFlow {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                val altitude = if (hasBarometer && isSeaLevelCalibrated) {
                    calculateBarometricAltitude(latestPressure)
                } else {
                    location.altitude
                }

                // Calibrate sea level pressure from GPS on first fix
                if (hasBarometer && !isSeaLevelCalibrated) {
                    calibrateSeaLevel(location.altitude, latestPressure)
                }

                // Convert WGS84 (Google GPS) to GCJ-02 (Amap coordinate system)
                val converter = CoordinateConverter(context)
                val gcj = converter
                    .from(CoordinateConverter.CoordType.GPS)
                    .coord(LatLng(location.latitude, location.longitude))
                    .convert()

                val speed = if (location.hasSpeed()) location.speed else 0f
                val accuracy = if (location.hasAccuracy()) location.accuracy else null
                val now = System.currentTimeMillis()

                val fix = GpsFix(
                    latitude = gcj.latitude,
                    longitude = gcj.longitude,
                    altitude = altitude,
                    speed = speed,
                    accuracy = accuracy,
                    timestamp = now
                )

                // Shift interpolation buffers
                prevGpsUpdate = lastGpsUpdate
                lastGpsUpdate = fix

                val update = LocationUpdate(
                    altitude = altitude,
                    speed = speed,
                    accuracy = accuracy,
                    gpsAltitude = location.altitude,
                    hasBarometer = hasBarometer,
                    latitude = gcj.latitude,
                    longitude = gcj.longitude
                )
                trySend(update)
            }
        }

        // Register barometer listener
        val pressureListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                latestPressure = event.values[0]
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (pressureSensor != null) {
            sensorManager.registerListener(
                pressureListener,
                pressureSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        @SuppressLint("MissingPermission")
        fusedLocationClient.requestLocationUpdates(locationRequest, callback, context.mainLooper)

        // Start interpolation coroutine
        val interpJob = launch(Dispatchers.Default) {
            interpolationLoop { interpolated -> trySend(interpolated) }
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
            if (pressureSensor != null) {
                sensorManager.unregisterListener(pressureListener)
            }
            interpJob.cancel()
        }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Interpolation loop: when the user is moving and we have two GPS fixes,
     * emit interpolated positions at ~200ms intervals between GPS updates.
     */
    private suspend fun interpolationLoop(onInterpolated: (LocationUpdate) -> Unit) {
        while (true) {
            delay(INTERPOLATION_INTERVAL_MS)

            val last = lastGpsUpdate ?: continue
            val prev = prevGpsUpdate ?: continue
            val now = System.currentTimeMillis()
            val elapsed = now - last.timestamp

            // Only interpolate between 200ms and 1000ms after the last GPS fix
            if (elapsed < INTERPOLATION_INTERVAL_MS || elapsed > 1200L) continue
            if (!movementDetector.isMoving.value) continue
            if (last.speed < MIN_INTERPOLATION_SPEED) continue

            // Time between the two GPS fixes
            val gpsDelta = (last.timestamp - prev.timestamp).toFloat()
            if (gpsDelta <= 0f || gpsDelta > 3000f) continue // sanity check

            // Fraction of time elapsed since last GPS fix (0.0 to ~1.0)
            val fraction = (elapsed.toFloat() / gpsDelta).coerceIn(0f, 1f)

            // Linear interpolation of position
            val lat = prev.latitude + (last.latitude - prev.latitude) * fraction
            val lon = prev.longitude + (last.longitude - prev.longitude) * fraction
            val alt = prev.altitude + (last.altitude - prev.altitude) * fraction

            // Accuracy degrades with time since last GPS fix
            val baseAccuracy = last.accuracy ?: 20f
            val interpolatedAccuracy = baseAccuracy + (elapsed.toFloat() / 1000f) * ACCURACY_DECAY_PER_SEC

            onInterpolated(
                LocationUpdate(
                    altitude = alt,
                    speed = last.speed,
                    accuracy = interpolatedAccuracy,
                    gpsAltitude = alt,
                    hasBarometer = false,
                    latitude = lat,
                    longitude = lon
                )
            )
        }
    }

    fun startBarometerUpdates() {
        // Barometer is already started in the callbackFlow initialization
    }

    fun stopBarometerUpdates() {
        // Handled by awaitClose in callbackFlow
    }

    /** Request a single fresh GPS fix (used by locate-me button) */
    @SuppressLint("MissingPermission")
    fun requestSingleLocation() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                location?.let {
                    val altitude = if (hasBarometer && isSeaLevelCalibrated) {
                        calculateBarometricAltitude(latestPressure)
                    } else {
                        it.altitude
                    }
                    scope.launch {
                        // The continuous updates already flow through locationUpdates,
                        // so this single request just ensures a quick re-center.
                    }
                }
            }
    }

    /**
     * Hypsometric formula: h = 44330 * (1 - (P/P0)^(1/5.255))
     */
    private fun calculateBarometricAltitude(pressure: Float): Double {
        return 44330.0 * (1.0 - (pressure.toDouble() / seaLevelPressure.toDouble()).pow(1.0 / 5.255))
    }

    /**
     * Calibrate sea-level pressure using known GPS altitude and current barometric pressure.
     * P0 = P * (1 - h/44330)^5.255
     */
    private fun calibrateSeaLevel(gpsAltitude: Double, pressure: Float) {
        seaLevelPressure = pressure * ((1.0 - gpsAltitude / 44330.0).pow(5.255)).toFloat()
        isSeaLevelCalibrated = true
    }

    /** Internal GPS fix snapshot for interpolation */
    private data class GpsFix(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double,
        val speed: Float,
        val accuracy: Float?,
        val timestamp: Long
    )

    companion object {
        private const val INTERPOLATION_INTERVAL_MS = 200L    // Emit interpolated points every 200ms
        private const val MIN_INTERPOLATION_SPEED = 1.5f      // Only interpolate above this speed (m/s)
        private const val ACCURACY_DECAY_PER_SEC = 5f         // Accuracy degrades 5m per second since last fix
    }
}

data class LocationUpdate(
    val altitude: Double,
    val speed: Float,
    val accuracy: Float?,
    val gpsAltitude: Double,
    val hasBarometer: Boolean,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
