// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.BuildConfig
import dev.anmitali.amble.R
import dev.anmitali.amble.data.HistoryExporter
import dev.anmitali.amble.data.db.StepSource
import dev.anmitali.amble.data.profile.Sex
import dev.anmitali.amble.service.StepTrackingService
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.components.AmbleButton
import dev.anmitali.amble.ui.components.BirthDatePicker
import dev.anmitali.amble.ui.components.RingGoalPicker
import dev.anmitali.amble.ui.currentLocale
import dev.anmitali.amble.ui.profile.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import kotlin.math.roundToInt

private const val AUTOSAVE_DEBOUNCE_MS = 600L

private enum class SettingsTab { BIODATA, GOAL, SENSORS, DATA, ABOUT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(application: AmbleApplication) {
    val viewModel = ambleViewModel { ProfileViewModel(application.profileStore) }
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    var tab by rememberSaveable { mutableIntStateOf(0) }

    var weightText by rememberSaveable { mutableStateOf(profile?.weightKg?.toString() ?: "") }
    var heightText by rememberSaveable { mutableStateOf(profile?.heightCm?.toString() ?: "") }
    var birthDateEpochDay by rememberSaveable {
        mutableLongStateOf((profile?.birthDate ?: LocalDate.now().minusYears(25)).toEpochDay())
    }
    var sexName by rememberSaveable { mutableStateOf((profile?.sex ?: Sex.MALE).name) }
    var stepGoal by rememberSaveable { mutableIntStateOf(profile?.dailyStepGoal ?: 10_000) }
    var distanceGoalKm by rememberSaveable { mutableFloatStateOf(profile?.dailyDistanceGoalKm ?: 5f) }
    var calorieGoal by rememberSaveable { mutableIntStateOf(profile?.dailyCalorieGoal ?: 300) }
    var strideText by rememberSaveable { mutableStateOf(profile?.strideLengthCm?.toString() ?: "") }

    val weight = weightText.toFloatOrNull()
    val height = heightText.toFloatOrNull()
    val birthDate = LocalDate.ofEpochDay(birthDateEpochDay)
    val sex = Sex.valueOf(sexName)
    val stride = strideText.toFloatOrNull()

    val weightValid = weight != null && weight in 20f..300f
    val heightValid = height != null && height in 100f..250f
    val strideValid = strideText.isEmpty() || (stride != null && stride in 30f..150f)

    val currentProfile = profile
    if (currentProfile != null && weightValid && heightValid) {
        LaunchedEffect(weight, height, birthDate, sex) {
            delay(AUTOSAVE_DEBOUNCE_MS)
            viewModel.save(currentProfile.copy(weightKg = weight!!, heightCm = height!!, birthDate = birthDate, sex = sex))
        }
    }
    if (currentProfile != null && strideValid) {
        LaunchedEffect(stepGoal, distanceGoalKm, calorieGoal, stride) {
            delay(AUTOSAVE_DEBOUNCE_MS)
            viewModel.save(
                currentProfile.copy(
                    dailyStepGoal = stepGoal,
                    dailyDistanceGoalKm = distanceGoalKm,
                    dailyCalorieGoal = calorieGoal,
                    strideLengthCm = stride,
                ),
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") }, windowInsets = WindowInsets(0, 0, 0, 0))
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            SecondaryScrollableTabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Biodata") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Goal") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Sensors") })
                Tab(selected = tab == 3, onClick = { tab = 3 }, text = { Text("Data") })
                Tab(selected = tab == 4, onClick = { tab = 4 }, text = { Text("About") })
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (SettingsTab.entries[tab]) {
                    SettingsTab.BIODATA -> BiodataTab(
                        weightText = weightText,
                        onWeightChange = { weightText = it },
                        weightValid = weightValid,
                        heightText = heightText,
                        onHeightChange = { heightText = it },
                        heightValid = heightValid,
                        birthDate = birthDate,
                        onBirthDateChange = { birthDateEpochDay = it.toEpochDay() },
                        sex = sex,
                        onSexChange = { sexName = it.name },
                    )
                    SettingsTab.GOAL -> GoalTab(
                        stepGoal = stepGoal,
                        onStepGoalChange = { stepGoal = it },
                        distanceGoalKm = distanceGoalKm,
                        onDistanceGoalChange = { distanceGoalKm = it },
                        calorieGoal = calorieGoal,
                        onCalorieGoalChange = { calorieGoal = it },
                        strideText = strideText,
                        onStrideChange = { strideText = it },
                        strideValid = strideValid,
                    )
                    SettingsTab.SENSORS -> SensorsTab(application)
                    SettingsTab.DATA -> DataTab(application)
                    SettingsTab.ABOUT -> AboutTab()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BiodataTab(
    weightText: String,
    onWeightChange: (String) -> Unit,
    weightValid: Boolean,
    heightText: String,
    onHeightChange: (String) -> Unit,
    heightValid: Boolean,
    birthDate: LocalDate,
    onBirthDateChange: (LocalDate) -> Unit,
    sex: Sex,
    onSexChange: (Sex) -> Unit,
) {
    OutlinedTextField(
        value = weightText,
        onValueChange = onWeightChange,
        label = { Text("Weight (kg)") },
        isError = weightText.isNotEmpty() && !weightValid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = heightText,
        onValueChange = onHeightChange,
        label = { Text("Height (cm)") },
        isError = heightText.isNotEmpty() && !heightValid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )

    BirthDatePicker(birthDate, onBirthDateChange)

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = sex == Sex.MALE,
            onClick = { onSexChange(Sex.MALE) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) { Text("Male") }
        SegmentedButton(
            selected = sex == Sex.FEMALE,
            onClick = { onSexChange(Sex.FEMALE) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) { Text("Female") }
    }
}

@Composable
private fun GoalTab(
    stepGoal: Int,
    onStepGoalChange: (Int) -> Unit,
    distanceGoalKm: Float,
    onDistanceGoalChange: (Float) -> Unit,
    calorieGoal: Int,
    onCalorieGoalChange: (Int) -> Unit,
    strideText: String,
    onStrideChange: (String) -> Unit,
    strideValid: Boolean,
) {
    Text("Daily step goal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        RingGoalPicker(
            value = stepGoal.toFloat(),
            onValueChange = { onStepGoalChange(it.roundToInt()) },
            valueRange = 2_000f..20_000f,
            step = 500f,
            valueText = "$stepGoal",
            label = "steps / day",
            diameter = 160.dp,
            strokeWidth = 14.dp,
        )
    }

    Text("Daily distance goal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        RingGoalPicker(
            value = distanceGoalKm,
            onValueChange = onDistanceGoalChange,
            valueRange = 1f..20f,
            step = 0.5f,
            valueText = String.format(currentLocale(), "%.1f", distanceGoalKm),
            label = "km / day",
            diameter = 160.dp,
            strokeWidth = 14.dp,
        )
    }

    Text("Daily calorie goal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        RingGoalPicker(
            value = calorieGoal.toFloat(),
            onValueChange = { onCalorieGoalChange(it.roundToInt()) },
            valueRange = 100f..1_500f,
            step = 50f,
            valueText = "$calorieGoal",
            label = "kcal / day",
            diameter = 160.dp,
            strokeWidth = 14.dp,
        )
    }

    OutlinedTextField(
        value = strideText,
        onValueChange = onStrideChange,
        label = { Text("Stride length override (cm, optional)") },
        isError = !strideValid,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SensorsTab(application: AmbleApplication) {
    val isTracking by StepTrackingService.isRunning.collectAsStateWithLifecycle()
    val activeSource by StepTrackingService.activeSource.collectAsStateWithLifecycle()

    Text("Background tracking", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Track steps in the background",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = isTracking,
            onCheckedChange = {
                if (it) StepTrackingService.start(application) else StepTrackingService.stop(application)
            },
        )
    }

    Text("Diagnostics", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Text(
        text = "Step source: " + sourceLabel(activeSource),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun sourceLabel(source: StepSource?): String = when (source) {
    StepSource.COUNTER -> "hardware step counter"
    StepSource.DETECTOR -> "hardware step detector"
    StepSource.ACCELEROMETER -> "accelerometer fallback (less accurate)"
    null -> "not tracking"
}

@Composable
private fun DataTab(application: AmbleApplication) {
    val scope = rememberCoroutineScope()
    var statusText by remember { mutableStateOf<String?>(null) }
    val exporter = remember { HistoryExporter(application, application.stepsRepository) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            statusText = try {
                exporter.exportTo(uri)
                "Exported."
            } catch (e: IOException) {
                "Export failed: ${e.message}"
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            statusText = try {
                val count = exporter.importFrom(uri)
                "Imported $count day(s)."
            } catch (e: Exception) {
                "Import failed: ${e.message}"
            }
        }
    }

    Text("Backup", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Text(
        "Export or import your step history as a single JSON file. Nothing leaves your device unless you share the file yourself.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    AmbleButton(
        text = "Export history",
        onClick = { exportLauncher.launch("amble-history-${LocalDate.now()}.json") },
    )
    AmbleButton(
        text = "Import history",
        onClick = { importLauncher.launch(arrayOf("application/json")) },
    )

    statusText?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AboutTab() {
    val context = LocalContext.current

    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
    Text(
        "Version ${BuildConfig.VERSION_NAME}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
        "A small, offline step tracker. No accounts, no ads, no analytics, no network access.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Surface(
        onClick = {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gnu.org/licenses/gpl-3.0.html")))
        },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            "Licensed under the GNU General Public License v3.0",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp),
        )
    }

    Text("Open source libraries", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OSS_LIBRARIES.forEach { library ->
            Surface(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(library.url))) },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(library.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(library.license, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(library.copyright, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
