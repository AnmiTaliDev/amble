// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.sensor

import kotlin.math.sqrt

class AccelerometerStepDetector(private val onStep: () -> Unit) {
    private val gravity = FloatArray(3)
    private var gravityInitialized = false
    private var lastStepTimestampNs = 0L
    private var aboveThreshold = false

    fun onSensorReading(x: Float, y: Float, z: Float, timestampNs: Long) {
        if (!gravityInitialized) {
            gravity[0] = x
            gravity[1] = y
            gravity[2] = z
            gravityInitialized = true
            return
        }

        gravity[0] = GRAVITY_FILTER_ALPHA * gravity[0] + (1 - GRAVITY_FILTER_ALPHA) * x
        gravity[1] = GRAVITY_FILTER_ALPHA * gravity[1] + (1 - GRAVITY_FILTER_ALPHA) * y
        gravity[2] = GRAVITY_FILTER_ALPHA * gravity[2] + (1 - GRAVITY_FILTER_ALPHA) * z

        val linearX = x - gravity[0]
        val linearY = y - gravity[1]
        val linearZ = z - gravity[2]
        val magnitude = sqrt(linearX * linearX + linearY * linearY + linearZ * linearZ)

        if (magnitude > STEP_THRESHOLD_MS2 && !aboveThreshold) {
            aboveThreshold = true
        } else if (magnitude < STEP_THRESHOLD_MS2 && aboveThreshold) {
            aboveThreshold = false
            val elapsedMs = (timestampNs - lastStepTimestampNs) / 1_000_000
            if (elapsedMs > MIN_STEP_INTERVAL_MS) {
                lastStepTimestampNs = timestampNs
                onStep()
            }
        }
    }

    private companion object {
        const val GRAVITY_FILTER_ALPHA = 0.8f

        const val STEP_THRESHOLD_MS2 = 2.0f

        const val MIN_STEP_INTERVAL_MS = 300L
    }
}
