// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data

import dev.anmitali.amble.data.db.DailyStepsDao
import dev.anmitali.amble.data.db.DailyStepsEntity
import dev.anmitali.amble.data.db.StepSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class StepsRepository(private val dao: DailyStepsDao) {

    fun observeDay(date: LocalDate): Flow<DailyStepsEntity?> = dao.observeByDate(date.toString())

    fun observeRecentDays(days: Int): Flow<List<DailyStepsEntity>> = dao.observeRecent(days)

    suspend fun applySensorReading(
        cumulativeSteps: Long,
        source: StepSource,
        today: LocalDate = LocalDate.now(),
    ): Int {
        val dateKey = today.toString()
        val existing = dao.getByDate(dateKey)

        val baseline = when {
            existing == null -> cumulativeSteps
            cumulativeSteps < existing.lastSensorCumulative -> cumulativeSteps - existing.steps
            else -> existing.sensorBaseline
        }

        val steps = (cumulativeSteps - baseline).toInt().coerceAtLeast(0)
        dao.upsert(
            DailyStepsEntity(
                date = dateKey,
                steps = steps,
                sensorBaseline = baseline,
                lastSensorCumulative = cumulativeSteps,
                source = source.name,
            ),
        )
        return steps
    }

    suspend fun addSteps(delta: Int, source: StepSource, today: LocalDate = LocalDate.now()): Int {
        val dateKey = today.toString()
        val existing = dao.getByDate(dateKey)
        val steps = (existing?.steps ?: 0) + delta
        dao.upsert(
            DailyStepsEntity(
                date = dateKey,
                steps = steps,
                sensorBaseline = existing?.sensorBaseline ?: 0L,
                lastSensorCumulative = existing?.lastSensorCumulative ?: 0L,
                source = source.name,
            ),
        )
        return steps
    }

    suspend fun getAllDays(): List<DailyStepsEntity> = dao.getAll()

    suspend fun importDay(date: LocalDate, steps: Int, source: StepSource) {
        val dateKey = date.toString()
        val existing = dao.getByDate(dateKey)
        dao.upsert(
            DailyStepsEntity(
                date = dateKey,
                steps = steps,
                sensorBaseline = existing?.sensorBaseline ?: 0L,
                lastSensorCumulative = existing?.lastSensorCumulative ?: 0L,
                source = source.name,
            ),
        )
    }
}
