// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data.profile

import java.time.LocalDate
import java.time.Period

enum class Sex {
    MALE,
    FEMALE,
}

data class UserProfile(
    val weightKg: Float,
    val heightCm: Float,
    val birthDate: LocalDate,
    val sex: Sex,
    val strideLengthCm: Float? = null,
    val dailyStepGoal: Int = 10_000,
    val dailyDistanceGoalKm: Float = 5f,
    val dailyCalorieGoal: Int = 300,
) {
    val age: Int get() = Period.between(birthDate, LocalDate.now()).years
}
