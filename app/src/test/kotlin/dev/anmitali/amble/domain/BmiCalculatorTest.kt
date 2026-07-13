// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BmiCalculatorTest {

    @Test
    fun `70kg at 175cm is a bmi of about 22,9`() {
        val bmi = BmiCalculator.bmi(weightKg = 70f, heightCm = 175f)
        assertEquals(22.86f, bmi, 0.01f)
        assertEquals(BmiCategory.NORMAL, BmiCalculator.category(bmi))
    }

    @Test
    fun `category thresholds`() {
        assertEquals(BmiCategory.UNDERWEIGHT, BmiCalculator.category(18.4f))
        assertEquals(BmiCategory.NORMAL, BmiCalculator.category(18.5f))
        assertEquals(BmiCategory.NORMAL, BmiCalculator.category(24.9f))
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.category(25f))
        assertEquals(BmiCategory.OVERWEIGHT, BmiCalculator.category(29.9f))
        assertEquals(BmiCategory.OBESE, BmiCalculator.category(30f))
    }
}
