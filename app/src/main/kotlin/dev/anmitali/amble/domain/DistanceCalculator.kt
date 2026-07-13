// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.domain

import dev.anmitali.amble.data.profile.Sex

object DistanceCalculator {

    private const val MALE_STRIDE_FACTOR = 0.415f
    private const val FEMALE_STRIDE_FACTOR = 0.413f

    fun strideLengthCm(heightCm: Float, sex: Sex): Float {
        val factor = if (sex == Sex.MALE) MALE_STRIDE_FACTOR else FEMALE_STRIDE_FACTOR
        return heightCm * factor
    }

    fun distanceKm(steps: Int, strideLengthCm: Float): Float {
        return steps * strideLengthCm / 100_000f
    }
}
