// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.currentLocale
import java.time.format.DateTimeFormatter

private enum class HistoryRange { DAILY, WEEKLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(application: AmbleApplication) {
    val viewModel = ambleViewModel { HistoryViewModel(application.stepsRepository, application.profileStore) }
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    var range by remember { mutableStateOf(HistoryRange.DAILY) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
    ) {
        Text(
            text = "History",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            SegmentedButton(
                selected = range == HistoryRange.DAILY,
                onClick = { range = HistoryRange.DAILY },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text("Daily") }
            SegmentedButton(
                selected = range == HistoryRange.WEEKLY,
                onClick = { range = HistoryRange.WEEKLY },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text("Weekly") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (range) {
                HistoryRange.DAILY -> items(dailyStats) { day ->
                    HistoryRow(
                        label = day.date.format(DAY_FORMATTER),
                        steps = day.steps,
                        distanceKm = day.distanceKm,
                        calories = day.calories,
                    )
                }
                HistoryRange.WEEKLY -> items(weeklyStats) { week ->
                    HistoryRow(
                        label = "Week of ${week.weekStart.format(DAY_FORMATTER)}",
                        steps = week.steps,
                        distanceKm = week.distanceKm,
                        calories = week.calories,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(label: String, steps: Int, distanceKm: Float, calories: Float) {
    val locale = currentLocale()
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Column {
                Text(
                    text = "$steps steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = String.format(locale, "%.2f km · %.0f kcal", distanceKm, calories),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val DAY_FORMATTER = DateTimeFormatter.ofPattern("d MMM")
