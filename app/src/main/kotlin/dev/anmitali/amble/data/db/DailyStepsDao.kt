// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStepsDao {
    @Query("SELECT * FROM daily_steps WHERE date = :date")
    suspend fun getByDate(date: String): DailyStepsEntity?

    @Query("SELECT * FROM daily_steps WHERE date = :date")
    fun observeByDate(date: String): Flow<DailyStepsEntity?>

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DailyStepsEntity>>

    @Query("SELECT * FROM daily_steps ORDER BY date ASC")
    suspend fun getAll(): List<DailyStepsEntity>

    @Upsert
    suspend fun upsert(entry: DailyStepsEntity)
}
