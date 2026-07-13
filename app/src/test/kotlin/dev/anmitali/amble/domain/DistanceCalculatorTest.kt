// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.domain

import dev.anmitali.amble.data.profile.Sex
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun `stride length scales with height`() {
        val stride = DistanceCalculator.strideLengthCm(heightCm = 180f, sex = Sex.MALE)
        assertEquals(74.7f, stride, 0.01f)
    }

    @Test
    fun `male and female stride factors differ slightly`() {
        val male = DistanceCalculator.strideLengthCm(heightCm = 170f, sex = Sex.MALE)
        val female = DistanceCalculator.strideLengthCm(heightCm = 170f, sex = Sex.FEMALE)
        assertEquals(70.55f, male, 0.01f)
        assertEquals(70.21f, female, 0.01f)
    }

    @Test
    fun `10000 steps at 75cm stride is 7,5km`() {
        val distance = DistanceCalculator.distanceKm(steps = 10_000, strideLengthCm = 75f)
        assertEquals(7.5f, distance, 0.001f)
    }

    @Test
    fun `zero steps means zero distance`() {
        assertEquals(0f, DistanceCalculator.distanceKm(steps = 0, strideLengthCm = 75f), 0f)
    }
}
