// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.domain

object CalorieCalculator {

    private const val KCAL_PER_STEP_PER_KG = 0.0005f

    private const val AGE_DECLINE_PER_YEAR = 0.002f
    private const val MIN_AGE_FACTOR = 0.8f
    private const val AGE_DECLINE_START = 30

    fun caloriesBurned(steps: Int, weightKg: Float, age: Int): Float {
        val ageFactor = (1f - (age - AGE_DECLINE_START).coerceAtLeast(0) * AGE_DECLINE_PER_YEAR)
            .coerceAtLeast(MIN_AGE_FACTOR)
        return steps * weightKg * KCAL_PER_STEP_PER_KG * ageFactor
    }
}
