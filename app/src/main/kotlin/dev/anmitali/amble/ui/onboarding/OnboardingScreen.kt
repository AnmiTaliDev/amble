// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.data.profile.Sex
import dev.anmitali.amble.data.profile.UserProfile
import dev.anmitali.amble.service.StepTrackingService
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.components.AmbleButton
import dev.anmitali.amble.ui.components.BirthDatePicker
import dev.anmitali.amble.ui.components.RingGoalPicker
import dev.anmitali.amble.ui.profile.ProfileViewModel
import dev.anmitali.amble.ui.theme.AccentColor
import java.time.LocalDate
import kotlin.math.roundToInt

private const val TOTAL_STEPS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(application: AmbleApplication, onFinished: () -> Unit) {
    val viewModel = ambleViewModel { ProfileViewModel(application.profileStore) }
    val context = LocalContext.current

    var step by rememberSaveable { mutableIntStateOf(0) }

    var weightKg by rememberSaveable { mutableFloatStateOf(70f) }
    var heightCm by rememberSaveable { mutableFloatStateOf(170f) }
    var birthDateEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().minusYears(25).toEpochDay()) }
    var sexName by rememberSaveable { mutableStateOf(Sex.MALE.name) }
    var stepGoal by rememberSaveable { mutableIntStateOf(10_000) }

    val birthDate = LocalDate.ofEpochDay(birthDateEpochDay)
    val sex = Sex.valueOf(sexName)

    fun finishOnboarding() {
        viewModel.save(
            UserProfile(
                weightKg = weightKg,
                heightCm = heightCm,
                birthDate = birthDate,
                sex = sex,
                dailyStepGoal = stepGoal,
            ),
        )
        StepTrackingService.start(application)
        onFinished()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { finishOnboarding() }

    fun requestPermissionsAndFinish() {
        val permissions = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.POST_NOTIFICATIONS)
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) finishOnboarding() else permissionLauncher.launch(permissions)
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        StepDots(current = step, total = TOTAL_STEPS)

        Column(modifier = Modifier.fillMaxSize().weight(1f).padding(24.dp)) {
            when (step) {
                0 -> WelcomeStep()
                1 -> WeightStep(weightKg) { weightKg = it }
                2 -> HeightStep(heightCm) { heightCm = it }
                3 -> BirthDateAndSexStep(birthDate, { birthDateEpochDay = it.toEpochDay() }, sex) { sexName = it.name }
                4 -> GoalStep(stepGoal) { stepGoal = it }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            AmbleButton(
                text = if (step == TOTAL_STEPS - 1) "Grant permissions and start" else "Continue",
                onClick = { if (step == TOTAL_STEPS - 1) requestPermissionsAndFinish() else step++ },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StepDots(current: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .background(
                        color = if (index == current) AccentColor else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text("Welcome to Amble", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        Text(
            "A few quick questions to estimate your distance and calories. Everything stays on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeightStep(weightKg: Float, onChange: (Float) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("What's your weight?", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(40.dp))
        Text("${weightKg.roundToInt()} kg", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(24.dp))
        Slider(value = weightKg, onValueChange = onChange, valueRange = 30f..200f, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun HeightStep(heightCm: Float, onChange: (Float) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("What's your height?", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(40.dp))
        Text("${heightCm.roundToInt()} cm", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(24.dp))
        Slider(value = heightCm, onValueChange = onChange, valueRange = 100f..220f, modifier = Modifier.fillMaxWidth())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDateAndSexStep(birthDate: LocalDate, onBirthDateChange: (LocalDate) -> Unit, sex: Sex, onSexChange: (Sex) -> Unit) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text("Birth date and sex", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Used to estimate calories burned. This never leaves your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        BirthDatePicker(birthDate, onBirthDateChange)

        Spacer(Modifier.height(16.dp))
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
}

@Composable
private fun GoalStep(goal: Int, onGoalChange: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Pick a daily step goal", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Drag around the ring. You can change this later in Settings.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        RingGoalPicker(
            value = goal.toFloat(),
            onValueChange = { onGoalChange(it.roundToInt()) },
            valueRange = 2_000f..20_000f,
            step = 500f,
            valueText = "$goal",
            label = "steps / day",
        )
    }
}
