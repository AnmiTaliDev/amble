// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.R
import dev.anmitali.amble.data.profile.Sex
import dev.anmitali.amble.data.profile.UserProfile
import dev.anmitali.amble.service.StepTrackingService
import dev.anmitali.amble.ui.ambleViewModel
import dev.anmitali.amble.ui.components.BirthDatePicker
import dev.anmitali.amble.ui.components.ConnectedChoiceRow
import dev.anmitali.amble.ui.components.GoalSlider
import dev.anmitali.amble.ui.profile.ProfileViewModel
import java.time.LocalDate
import kotlin.math.roundToInt

private const val TOTAL_STEPS = 5

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

    BackHandler(enabled = step > 0) { step-- }

    val progress by animateFloatAsState(
        targetValue = (step + 1) / TOTAL_STEPS.toFloat(),
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
    )
    val isLastStep = step == TOTAL_STEPS - 1

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 24.dp)
                    .heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    if (step > 0) {
                        IconButton(onClick = { step-- }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
                Spacer(Modifier.size(8.dp))
                LinearWavyProgressIndicator(progress = { progress }, modifier = Modifier.weight(1f))
            }
        },
        bottomBar = {
            Button(
                onClick = { if (isLastStep) requestPermissionsAndFinish() else step++ },
                shapes = ButtonDefaults.shapesFor(ButtonDefaults.MediumContainerHeight),
                contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .heightIn(min = ButtonDefaults.MediumContainerHeight),
            ) {
                Text(
                    text = if (isLastStep) "Grant permissions and start" else "Continue",
                    style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight),
                )
            }
        },
    ) { innerPadding ->
        val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
        val fadeSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(slideSpec) { width -> direction * width / 4 } + fadeIn(fadeSpec)) togetherWith
                    (slideOutHorizontally(slideSpec) { width -> -direction * width / 4 } + fadeOut(fadeSpec))
            },
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            label = "onboardingStep",
        ) { currentStep ->
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
                    when (currentStep) {
                        0 -> WelcomeStep()
                        1 -> WeightStep(weightKg) { weightKg = it }
                        2 -> HeightStep(heightCm) { heightCm = it }
                        3 -> BirthDateAndSexStep(birthDate, { birthDateEpochDay = it.toEpochDay() }, sex) { sexName = it.name }
                        4 -> GoalStep(stepGoal) { stepGoal = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTitle(title: String, subtitle: String? = null) {
    Text(title, style = MaterialTheme.typography.headlineLargeEmphasized, color = MaterialTheme.colorScheme.onSurface)
    if (subtitle != null) {
        Spacer(Modifier.size(8.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Spacer(Modifier.size(32.dp))
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WelcomeStep() {
    Box(
        modifier = Modifier
            .size(160.dp)
            .clip(MaterialShapes.SoftBurst.toShape())
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_notification_steps),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(80.dp),
        )
    }
    Spacer(Modifier.size(40.dp))
    Text("Welcome to Amble", style = MaterialTheme.typography.displaySmallEmphasized, color = MaterialTheme.colorScheme.onSurface)
    Spacer(Modifier.size(12.dp))
    Text(
        "A few quick questions to estimate your distance and calories. Everything stays on your device.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun WeightStep(weightKg: Float, onChange: (Float) -> Unit) {
    StepTitle("What's your weight?")
    GoalSlider(
        value = weightKg,
        onValueChange = onChange,
        valueRange = 30f..200f,
        step = 1f,
        valueText = "${weightKg.roundToInt()}",
        unit = "kg",
    )
}

@Composable
private fun HeightStep(heightCm: Float, onChange: (Float) -> Unit) {
    StepTitle("What's your height?")
    GoalSlider(
        value = heightCm,
        onValueChange = onChange,
        valueRange = 100f..220f,
        step = 1f,
        valueText = "${heightCm.roundToInt()}",
        unit = "cm",
    )
}

@Composable
private fun BirthDateAndSexStep(birthDate: LocalDate, onBirthDateChange: (LocalDate) -> Unit, sex: Sex, onSexChange: (Sex) -> Unit) {
    StepTitle("Birth date and sex", "Used to estimate calories burned. This never leaves your device.")
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BirthDatePicker(birthDate, onBirthDateChange)
        ConnectedChoiceRow(
            options = Sex.entries,
            selected = sex,
            onSelect = onSexChange,
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
        )
    }
}

@Composable
private fun GoalStep(goal: Int, onGoalChange: (Int) -> Unit) {
    StepTitle("Pick a daily step goal", "You can change this later in Settings.")
    GoalSlider(
        value = goal.toFloat(),
        onValueChange = { onGoalChange(it.roundToInt()) },
        valueRange = 2_000f..20_000f,
        step = 500f,
        valueText = "$goal",
        unit = "steps / day",
    )
}
