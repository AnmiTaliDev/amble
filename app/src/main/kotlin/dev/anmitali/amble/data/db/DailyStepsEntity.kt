// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailyStepsEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val sensorBaseline: Long,
    val lastSensorCumulative: Long,
    val source: String,
)
