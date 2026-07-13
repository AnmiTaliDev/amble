// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun `10000 steps at 70kg lands in the expected ballpark`() {
        val calories = CalorieCalculator.caloriesBurned(steps = 10_000, weightKg = 70f, age = 30)
        assertEquals(350f, calories, 0.5f)
    }

    @Test
    fun `zero steps burns zero calories`() {
        assertEquals(0f, CalorieCalculator.caloriesBurned(steps = 0, weightKg = 70f, age = 30), 0f)
    }

    @Test
    fun `heavier person burns more for the same steps`() {
        val lighter = CalorieCalculator.caloriesBurned(steps = 5_000, weightKg = 60f, age = 30)
        val heavier = CalorieCalculator.caloriesBurned(steps = 5_000, weightKg = 90f, age = 30)
        assertTrue(heavier > lighter)
    }

    @Test
    fun `age factor never drops below the floor`() {
        val young = CalorieCalculator.caloriesBurned(steps = 5_000, weightKg = 70f, age = 30)
        val old = CalorieCalculator.caloriesBurned(steps = 5_000, weightKg = 70f, age = 200)
        assertEquals(young * 0.8f, old, 0.01f)
    }
}
