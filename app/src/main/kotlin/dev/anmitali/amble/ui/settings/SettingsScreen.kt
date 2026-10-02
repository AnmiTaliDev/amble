// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
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
import dev.anmitali.amble.ui.components.BirthDatePicker
import dev.anmitali.amble.ui.components.ConnectedChoiceRow
import dev.anmitali.amble.ui.components.GoalSlider
import dev.anmitali.amble.ui.currentLocale
import dev.anmitali.amble.ui.profile.ProfileViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import kotlin.math.roundToInt

private const val AUTOSAVE_DEBOUNCE_MS = 600L

private enum class SettingsTab(val label: String) {
    BIODATA("Biodata"),
    GOAL("Goal"),
    SENSORS("Sensors"),
    DATA("Data"),
    ABOUT("About"),
}

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

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryScrollableTabRow(selectedTabIndex = tab, edgePadding = 16.dp) {
                SettingsTab.entries.forEachIndexed { index, settingsTab ->
                    Tab(selected = tab == index, onClick = { tab = index }, text = { Text(settingsTab.label) })
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .padding(16.dp),
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
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
    )
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
    SectionHeader("Body")
    OutlinedTextField(
        value = weightText,
        onValueChange = onWeightChange,
        label = { Text("Weight") },
        suffix = { Text("kg") },
        isError = weightText.isNotEmpty() && !weightValid,
        supportingText = { Text("20 to 300 kg") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = heightText,
        onValueChange = onHeightChange,
        label = { Text("Height") },
        suffix = { Text("cm") },
        isError = heightText.isNotEmpty() && !heightValid,
        supportingText = { Text("100 to 250 cm") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )

    SectionHeader("Personal")
    BirthDatePicker(birthDate, onBirthDateChange)
    ConnectedChoiceRow(
        options = Sex.entries,
        selected = sex,
        onSelect = onSexChange,
        label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
    )
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
    SectionHeader("Daily steps")
    GoalCard {
        GoalSlider(
            value = stepGoal.toFloat(),
            onValueChange = { onStepGoalChange(it.roundToInt()) },
            valueRange = 2_000f..20_000f,
            step = 500f,
            valueText = "$stepGoal",
            unit = "steps",
        )
    }

    SectionHeader("Daily distance")
    GoalCard {
        GoalSlider(
            value = distanceGoalKm,
            onValueChange = onDistanceGoalChange,
            valueRange = 1f..20f,
            step = 0.5f,
            valueText = String.format(currentLocale(), "%.1f", distanceGoalKm),
            unit = "km",
        )
    }

    SectionHeader("Daily calories")
    GoalCard {
        GoalSlider(
            value = calorieGoal.toFloat(),
            onValueChange = { onCalorieGoalChange(it.roundToInt()) },
            valueRange = 100f..1_500f,
            step = 50f,
            valueText = "$calorieGoal",
            unit = "kcal",
        )
    }

    SectionHeader("Stride")
    OutlinedTextField(
        value = strideText,
        onValueChange = onStrideChange,
        label = { Text("Stride length override") },
        suffix = { Text("cm") },
        isError = !strideValid,
        supportingText = { Text("Optional, 30 to 150 cm. Leave empty to estimate from height.") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GoalCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.largeIncreased,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(modifier = Modifier.padding(20.dp)) { content() }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SensorsTab(application: AmbleApplication) {
    val isTracking by StepTrackingService.isRunning.collectAsStateWithLifecycle()
    val activeSource by StepTrackingService.activeSource.collectAsStateWithLifecycle()

    fun setTracking(enabled: Boolean) {
        if (enabled) StepTrackingService.start(application) else StepTrackingService.stop(application)
    }

    SectionHeader("Background tracking")
    SegmentedListItem(
        onClick = { setTracking(!isTracking) },
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        supportingContent = { Text("Keeps counting while the app is closed") },
        trailingContent = { Switch(checked = isTracking, onCheckedChange = ::setTracking) },
    ) {
        Text("Track steps in the background")
    }

    SectionHeader("Diagnostics")
    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        overlineContent = { Text("Step source") },
    ) {
        Text(sourceLabel(activeSource).replaceFirstChar(Char::uppercase))
    }
}

private fun sourceLabel(source: StepSource?): String = when (source) {
    StepSource.COUNTER -> "hardware step counter"
    StepSource.DETECTOR -> "hardware step detector"
    StepSource.ACCELEROMETER -> "accelerometer fallback (less accurate)"
    null -> "not tracking"
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    SectionHeader("Backup")
    Text(
        "Export or import your step history as a single JSON file. Nothing leaves your device unless you share the file yourself.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp),
    )

    Button(
        onClick = { exportLauncher.launch("amble-history-${LocalDate.now()}.json") },
        shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
    ) {
        Text("Export history", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
    }
    FilledTonalButton(
        onClick = { importLauncher.launch(arrayOf("application/json")) },
        shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
        contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight),
    ) {
        Text("Import history", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
    }

    statusText?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AboutTab() {
    val context = LocalContext.current

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMediumEmphasized)
            Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelLarge)
            Text(
                "A small, offline step tracker. No accounts, no ads, no analytics, no network access.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }

    SectionHeader("License")
    SegmentedListItem(
        onClick = { openUrl("https://www.gnu.org/licenses/gpl-3.0.html") },
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        supportingContent = { Text("GNU General Public License v3.0") },
    ) {
        Text("Licensed under GPL-3.0")
    }

    SectionHeader("Open source libraries")
    Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        OSS_LIBRARIES.forEachIndexed { index, library ->
            SegmentedListItem(
                onClick = { openUrl(library.url) },
                shapes = ListItemDefaults.segmentedShapes(index = index, count = OSS_LIBRARIES.size),
                overlineContent = { Text(library.license) },
                supportingContent = { Text(library.copyright) },
            ) {
                Text(library.name)
            }
        }
    }
}
