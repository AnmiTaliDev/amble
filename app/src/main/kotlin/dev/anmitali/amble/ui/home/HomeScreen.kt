// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.R
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.components.StatTile
import dev.anmitali.amble.ui.currentLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(application: AmbleApplication) {
    val viewModel = ambleViewModel {
        HomeViewModel(application.applicationContext, application.stepsRepository, application.profileStore)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val locale = currentLocale()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Today") },
                subtitle = { Text(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))) },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                StepsHero(steps = uiState.steps, goal = uiState.dailyStepGoal)

                val toggleHeight = ButtonDefaults.MediumContainerHeight
                ToggleButton(
                    checked = isTracking,
                    onCheckedChange = { if (it) viewModel.startTracking() else viewModel.stopTracking() },
                    shapes = ToggleButtonDefaults.shapesFor(toggleHeight),
                    contentPadding = ButtonDefaults.contentPaddingFor(toggleHeight, hasStartIcon = true),
                    modifier = Modifier.heightIn(min = toggleHeight),
                ) {
                    val iconModifier = Modifier.size(ButtonDefaults.iconSizeFor(toggleHeight))
                    if (isTracking) {
                        Icon(painterResource(R.drawable.ic_pause), contentDescription = null, modifier = iconModifier)
                    } else {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = iconModifier)
                    }
                    Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(toggleHeight)))
                    Text(if (isTracking) "Tracking" else "Paused", style = ButtonDefaults.textStyleFor(toggleHeight))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        label = "Calories",
                        value = String.format(locale, "%.0f", uiState.calories),
                        unit = "kcal",
                        goalText = "of ${uiState.dailyCalorieGoal} kcal",
                        progress = if (uiState.dailyCalorieGoal > 0) uiState.calories / uiState.dailyCalorieGoal else 0f,
                        modifier = Modifier.weight(1f),
                    )
                    MetricCard(
                        label = "Distance",
                        value = String.format(locale, "%.1f", uiState.distanceKm),
                        unit = "km",
                        goalText = String.format(locale, "of %.1f km", uiState.dailyDistanceGoalKm),
                        progress = if (uiState.dailyDistanceGoalKm > 0) uiState.distanceKm / uiState.dailyDistanceGoalKm else 0f,
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile(modifier = Modifier.weight(1f), label = "7-day average", value = "${uiState.weekAverageSteps}")
                    StatTile(modifier = Modifier.weight(1f), label = "Best day", value = "${uiState.bestDaySteps}")
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = "BMI",
                        value = uiState.bmi?.let { String.format(locale, "%.1f", it) } ?: "-",
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StepsHero(steps: Int, goal: Int) {
    val target = if (goal > 0) (steps / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val progress by animateFloatAsState(target, animationSpec = MaterialTheme.motionScheme.slowSpatialSpec())
    val strokePx = with(LocalDensity.current) { 16.dp.toPx() }
    val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
        CircularWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(280.dp),
            stroke = stroke,
            trackStroke = stroke,
            wavelength = 40.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$steps",
                style = MaterialTheme.typography.displayLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "of $goal steps",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MetricCard(label: String, value: String, unit: String, goalText: String, progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), animationSpec = MaterialTheme.motionScheme.slowSpatialSpec())

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(value, style = MaterialTheme.typography.headlineLargeEmphasized, modifier = Modifier.alignByBaseline())
                Text(unit, style = MaterialTheme.typography.titleSmall, modifier = Modifier.alignByBaseline())
            }
            LinearWavyProgressIndicator(
                progress = { animated },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            )
            Text(goalText, style = MaterialTheme.typography.bodySmall)
        }
    }
}
