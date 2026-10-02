// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.components.ConnectedChoiceRow
import dev.anmitali.amble.ui.currentLocale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class HistoryRange(val label: String) { DAILY("Daily"), WEEKLY("Weekly") }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(application: AmbleApplication) {
    val viewModel = ambleViewModel { HistoryViewModel(application.stepsRepository, application.profileStore) }
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStats.collectAsStateWithLifecycle()
    var range by remember { mutableStateOf(HistoryRange.DAILY) }
    val locale = currentLocale()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { LargeFlexibleTopAppBar(title = { Text("History") }, scrollBehavior = scrollBehavior) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                item {
                    ConnectedChoiceRow(
                        options = HistoryRange.entries,
                        selected = range,
                        onSelect = { range = it },
                        label = { it.label },
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
                when (range) {
                    HistoryRange.DAILY -> itemsIndexed(dailyStats) { index, day ->
                        HistoryRow(
                            index = index,
                            count = dailyStats.size,
                            date = day.date,
                            headline = day.date.format(DateTimeFormatter.ofPattern("EEEE", locale)),
                            steps = day.steps,
                            distanceKm = day.distanceKm,
                            calories = day.calories,
                        )
                    }
                    HistoryRange.WEEKLY -> itemsIndexed(weeklyStats) { index, week ->
                        HistoryRow(
                            index = index,
                            count = weeklyStats.size,
                            date = week.weekStart,
                            headline = "Week of ${week.weekStart.format(DateTimeFormatter.ofPattern("d MMM", locale))}",
                            steps = week.steps,
                            distanceKm = week.distanceKm,
                            calories = week.calories,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HistoryRow(
    index: Int,
    count: Int,
    date: LocalDate,
    headline: String,
    steps: Int,
    distanceKm: Float,
    calories: Float,
) {
    val locale = currentLocale()
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        leadingContent = { DateBadge(date) },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text("$steps", style = MaterialTheme.typography.titleMediumEmphasized, color = MaterialTheme.colorScheme.onSurface)
                Text("steps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        supportingContent = { Text(String.format(locale, "%.2f km · %.0f kcal", distanceKm, calories)) },
    ) {
        Text(headline)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DateBadge(date: LocalDate) {
    val isToday = date == LocalDate.now()
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${date.dayOfMonth}",
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
