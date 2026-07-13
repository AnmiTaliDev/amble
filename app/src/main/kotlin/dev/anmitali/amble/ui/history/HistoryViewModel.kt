// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anmitali.amble.data.StepsRepository
import dev.anmitali.amble.data.db.DailyStepsEntity
import dev.anmitali.amble.data.profile.ProfileStore
import dev.anmitali.amble.data.profile.UserProfile
import dev.anmitali.amble.domain.CalorieCalculator
import dev.anmitali.amble.domain.DistanceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

data class DayStat(val date: LocalDate, val steps: Int, val distanceKm: Float, val calories: Float)
data class WeekStat(val weekStart: LocalDate, val steps: Int, val distanceKm: Float, val calories: Float)

class HistoryViewModel(
    stepsRepository: StepsRepository,
    profileStore: ProfileStore,
) : ViewModel() {

    val dailyStats: StateFlow<List<DayStat>> = combine(
        stepsRepository.observeRecentDays(RECENT_DAYS_WINDOW),
        profileStore.profile,
    ) { days, profile -> days.map { it.toDayStat(profile) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val weeklyStats: StateFlow<List<WeekStat>> = dailyStats.map { days ->
        days.groupBy { it.date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
            .map { (weekStart, entries) ->
                WeekStat(
                    weekStart = weekStart,
                    steps = entries.sumOf { it.steps },
                    distanceKm = entries.sumOf { it.distanceKm.toDouble() }.toFloat(),
                    calories = entries.sumOf { it.calories.toDouble() }.toFloat(),
                )
            }
            .sortedByDescending { it.weekStart }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private companion object {
        const val RECENT_DAYS_WINDOW = 30
    }
}

private fun DailyStepsEntity.toDayStat(profile: UserProfile?): DayStat {
    val parsedDate = LocalDate.parse(date)
    if (profile == null) return DayStat(parsedDate, steps, 0f, 0f)

    val strideLengthCm = profile.strideLengthCm ?: DistanceCalculator.strideLengthCm(profile.heightCm, profile.sex)
    return DayStat(
        date = parsedDate,
        steps = steps,
        distanceKm = DistanceCalculator.distanceKm(steps, strideLengthCm),
        calories = CalorieCalculator.caloriesBurned(steps, profile.weightKg, profile.age),
    )
}
