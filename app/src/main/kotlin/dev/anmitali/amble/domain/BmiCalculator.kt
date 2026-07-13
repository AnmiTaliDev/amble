// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.domain

enum class BmiCategory {
    UNDERWEIGHT,
    NORMAL,
    OVERWEIGHT,
    OBESE,
}

object BmiCalculator {

    fun bmi(weightKg: Float, heightCm: Float): Float {
        val heightM = heightCm / 100f
        return weightKg / (heightM * heightM)
    }

    fun category(bmi: Float): BmiCategory = when {
        bmi < 18.5f -> BmiCategory.UNDERWEIGHT
        bmi < 25f -> BmiCategory.NORMAL
        bmi < 30f -> BmiCategory.OVERWEIGHT
        else -> BmiCategory.OBESE
    }
}
