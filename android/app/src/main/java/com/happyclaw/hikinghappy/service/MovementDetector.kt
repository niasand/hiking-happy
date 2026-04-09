package com.happyclaw.hikinghappy.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Detects whether the user is currently moving (walking/running) using the
 * device's linear acceleration sensor (gravity-free accelerometer).
 *
 * Uses a sliding-window variance approach: when variance exceeds a threshold
 * the user is considered to be moving. This is lightweight and works well
 * for hiking/walking detection.
 */
@Singleton
class MovementDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val linearAccelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Sliding window of acceleration magnitudes
    private val accelBuffer = FloatArray(WINDOW_SIZE)
    private var bufferIndex = 0
    private var bufferFilled = false

    // Exponential moving average for smoother transitions
    private var smoothedVariance = 0.0
    private var lastMovingState = false

    val isMoving: StateFlow<Boolean> = MutableStateFlow(false)

    private val _isMovingMutable = MutableStateFlow(false)

    fun start() {
        val sensor = linearAccelSensor ?: return
        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_UI // ~60ms, sufficient for step detection
        )
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
        _isMovingMutable.value = false
        lastMovingState = false
        bufferIndex = 0
        bufferFilled = false
        smoothedVariance = 0.0
    }

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt(x * x + y * y + z * z)

            accelBuffer[bufferIndex] = magnitude
            bufferIndex = (bufferIndex + 1) % WINDOW_SIZE
            if (bufferIndex == 0) bufferFilled = true

            val count = if (bufferFilled) WINDOW_SIZE else bufferIndex
            if (count < MIN_SAMPLES) return

            // Compute variance of the buffer
            var sum = 0.0
            for (i in 0 until count) sum += accelBuffer[i]
            val mean = sum / count

            var variance = 0.0
            for (i in 0 until count) {
                val diff = accelBuffer[i] - mean
                variance += diff * diff
            }
            variance /= count

            // Exponential smoothing for the variance signal
            smoothedVariance = SMOOTHING_ALPHA * variance + (1 - SMOOTHING_ALPHA) * smoothedVariance

            // Hysteresis to prevent rapid toggling
            val moving = if (lastMovingState) {
                smoothedVariance > VARIANCE_THRESHOLD_LOW
            } else {
                smoothedVariance > VARIANCE_THRESHOLD_HIGH
            }

            if (moving != lastMovingState) {
                lastMovingState = moving
                _isMovingMutable.value = moving
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    companion object {
        private const val WINDOW_SIZE = 15       // ~1 second at 60ms sensor delay
        private const val MIN_SAMPLES = 8        // Need at least 8 samples before computing variance
        private const val VARIANCE_THRESHOLD_HIGH = 2.5  // Start considering "moving" above this
        private const val VARIANCE_THRESHOLD_LOW = 1.0   // Stop considering "moving" below this (hysteresis)
        private const val SMOOTHING_ALPHA = 0.3          // Eponential smoothing factor
    }
}
