// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.domain.BmiCalculator
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.components.StatTile
import dev.anmitali.amble.ui.currentLocale
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(application: AmbleApplication) {
    val viewModel = ambleViewModel { ProfileViewModel(application.profileStore) }
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val locale = currentLocale()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(88.dp)) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
            )
        }

        if (profile == null) {
            Text(
                text = "No data yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Column
        }
        val currentProfile = profile!!

        val bmi = BmiCalculator.bmi(currentProfile.weightKg, currentProfile.heightCm)
        val bmiCategory = BmiCalculator.category(bmi).name.lowercase().replaceFirstChar(Char::uppercase)

        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = String.format(locale, "%.1f", bmi),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "BMI · $bmiCategory",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(modifier = Modifier.weight(1f), label = "Weight", value = "${currentProfile.weightKg.toInt()} kg")
                StatTile(modifier = Modifier.weight(1f), label = "Height", value = "${currentProfile.heightCm.toInt()} cm")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatTile(modifier = Modifier.weight(1f), label = "Age", value = "${currentProfile.age}")
                StatTile(
                    modifier = Modifier.weight(1f),
                    label = "Sex",
                    value = currentProfile.sex.name.lowercase().replaceFirstChar(Char::uppercase),
                )
            }
            StatTile(
                modifier = Modifier.fillMaxWidth(),
                label = "Birth date",
                value = currentProfile.birthDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")),
            )
        }

        Text(
            text = "Daily goals",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatTile(modifier = Modifier.weight(1f), label = "Steps", value = "${currentProfile.dailyStepGoal}")
            StatTile(
                modifier = Modifier.weight(1f),
                label = "Distance",
                value = String.format(locale, "%.1f km", currentProfile.dailyDistanceGoalKm),
            )
            StatTile(modifier = Modifier.weight(1f), label = "Calories", value = "${currentProfile.dailyCalorieGoal} kcal")
        }

        Text(
            text = "Edit these in Settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
