// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.R
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.components.RingGauge
import dev.anmitali.amble.ui.components.StatTile
import dev.anmitali.amble.ui.currentLocale

@Composable
fun HomeScreen(application: AmbleApplication) {
    val viewModel = ambleViewModel {
        HomeViewModel(application.applicationContext, application.stepsRepository, application.profileStore)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            IconButton(onClick = { if (isTracking) viewModel.stopTracking() else viewModel.startTracking() }) {
                if (isTracking) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pause),
                        contentDescription = "Pause tracking",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Resume tracking",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        val goalProgress = if (uiState.dailyStepGoal > 0) uiState.steps / uiState.dailyStepGoal.toFloat() else 0f
        RingGauge(progress = goalProgress, diameter = 240.dp, strokeWidth = 18.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${uiState.steps}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "of ${uiState.dailyStepGoal} steps",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            val calorieProgress = if (uiState.dailyCalorieGoal > 0) uiState.calories / uiState.dailyCalorieGoal else 0f
            RingGauge(progress = calorieProgress, diameter = 84.dp, strokeWidth = 8.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(currentLocale(), "%.0f", uiState.calories), style = MaterialTheme.typography.titleMedium)
                    Text("kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            val distanceProgress = if (uiState.dailyDistanceGoalKm > 0) uiState.distanceKm / uiState.dailyDistanceGoalKm else 0f
            RingGauge(progress = distanceProgress, diameter = 84.dp, strokeWidth = 8.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(String.format(currentLocale(), "%.1f", uiState.distanceKm), style = MaterialTheme.typography.titleMedium)
                    Text("km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(modifier = Modifier.weight(1f), label = "7-day average", value = "${uiState.weekAverageSteps} steps")
                StatTile(modifier = Modifier.weight(1f), label = "Best day", value = "${uiState.bestDaySteps} steps")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    label = "BMI",
                    value = uiState.bmi?.let { String.format(currentLocale(), "%.1f", it) } ?: "—",
                )
                StatTile(modifier = Modifier.weight(1f), label = "Tracking", value = if (isTracking) "Active" else "Paused")
            }
        }
    }
}
