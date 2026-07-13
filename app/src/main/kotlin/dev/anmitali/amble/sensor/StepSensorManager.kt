// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import dev.anmitali.amble.data.db.StepSource

class StepSensorManager(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val counterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var accelerometerStepDetector: AccelerometerStepDetector? = null
    private var onCounterReading: ((Long) -> Unit)? = null
    private var onStepDetected: (() -> Unit)? = null

    var activeSource: StepSource = StepSource.ACCELEROMETER
        private set

    fun start(onCounterReading: (cumulativeSteps: Long) -> Unit, onStepDetected: () -> Unit) {
        stop()
        this.onCounterReading = onCounterReading
        this.onStepDetected = onStepDetected

        val hasActivityRecognitionPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED

        activeSource = when {
            counterSensor != null && hasActivityRecognitionPermission -> StepSource.COUNTER
            detectorSensor != null && hasActivityRecognitionPermission -> StepSource.DETECTOR
            else -> StepSource.ACCELEROMETER
        }

        when (activeSource) {
            StepSource.COUNTER -> {
                sensorManager.registerListener(this, counterSensor, SensorManager.SENSOR_DELAY_NORMAL)
                detectorSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
            }

            StepSource.DETECTOR -> {
                sensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
            }

            StepSource.ACCELEROMETER -> {
                if (accelerometerSensor != null) {
                    accelerometerStepDetector = AccelerometerStepDetector { onStepDetected() }
                    sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
                }
            }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        accelerometerStepDetector = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> onCounterReading?.invoke(event.values[0].toLong())
            Sensor.TYPE_STEP_DETECTOR -> onStepDetected?.invoke()
            Sensor.TYPE_ACCELEROMETER -> accelerometerStepDetector?.onSensorReading(
                event.values[0],
                event.values[1],
                event.values[2],
                event.timestamp,
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
