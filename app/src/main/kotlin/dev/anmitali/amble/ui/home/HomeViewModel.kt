// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anmitali.amble.data.StepsRepository
import dev.anmitali.amble.data.profile.ProfileStore
import dev.anmitali.amble.domain.BmiCalculator
import dev.anmitali.amble.domain.CalorieCalculator
import dev.anmitali.amble.domain.DistanceCalculator
import dev.anmitali.amble.service.StepTrackingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class HomeUiState(
    val steps: Int = 0,
    val distanceKm: Float = 0f,
    val calories: Float = 0f,
    val hasProfile: Boolean = false,
    val dailyStepGoal: Int = 10_000,
    val dailyDistanceGoalKm: Float = 5f,
    val dailyCalorieGoal: Int = 300,
    val weekAverageSteps: Int = 0,
    val bestDaySteps: Int = 0,
    val bmi: Float? = null,
)

class HomeViewModel(
    private val appContext: Context,
    stepsRepository: StepsRepository,
    profileStore: ProfileStore,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        stepsRepository.observeDay(LocalDate.now()),
        profileStore.profile,
        stepsRepository.observeRecentDays(7),
    ) { entry, profile, recentDays ->
        val steps = entry?.steps ?: 0
        val weekAverageSteps = if (recentDays.isEmpty()) 0 else recentDays.sumOf { it.steps } / recentDays.size
        val bestDaySteps = recentDays.maxOfOrNull { it.steps } ?: 0

        if (profile == null) {
            HomeUiState(steps = steps, weekAverageSteps = weekAverageSteps, bestDaySteps = bestDaySteps)
        } else {
            val strideLengthCm = profile.strideLengthCm
                ?: DistanceCalculator.strideLengthCm(profile.heightCm, profile.sex)
            HomeUiState(
                steps = steps,
                distanceKm = DistanceCalculator.distanceKm(steps, strideLengthCm),
                calories = CalorieCalculator.caloriesBurned(steps, profile.weightKg, profile.age),
                hasProfile = true,
                dailyStepGoal = profile.dailyStepGoal,
                dailyDistanceGoalKm = profile.dailyDistanceGoalKm,
                dailyCalorieGoal = profile.dailyCalorieGoal,
                weekAverageSteps = weekAverageSteps,
                bestDaySteps = bestDaySteps,
                bmi = BmiCalculator.bmi(profile.weightKg, profile.heightCm),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    val isTracking: StateFlow<Boolean> = StepTrackingService.isRunning

    fun startTracking() = StepTrackingService.start(appContext)

    fun stopTracking() = StepTrackingService.stop(appContext)
}
