// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.widget

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.domain.CalorieCalculator
import dev.anmitali.amble.domain.DistanceCalculator
import dev.anmitali.amble.ui.theme.BackgroundDeep
import dev.anmitali.amble.ui.theme.RingTrack
import dev.anmitali.amble.ui.theme.TextMuted
import dev.anmitali.amble.ui.theme.TextPrimary
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.Locale

class StepsWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(100.dp, 48.dp), DpSize(180.dp, 110.dp), DpSize(250.dp, 180.dp)),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as AmbleApplication
        val today = app.stepsRepository.observeDay(LocalDate.now()).first()
        val profile = app.profileStore.profile.value
        val steps = today?.steps ?: 0

        val distanceKm: Float
        val calories: Float
        val goalProgress: Float
        if (profile != null) {
            val strideLengthCm = profile.strideLengthCm
                ?: DistanceCalculator.strideLengthCm(profile.heightCm, profile.sex)
            distanceKm = DistanceCalculator.distanceKm(steps, strideLengthCm)
            calories = CalorieCalculator.caloriesBurned(steps, profile.weightKg, profile.age)
            goalProgress = if (profile.dailyStepGoal > 0) steps / profile.dailyStepGoal.toFloat() else 0f
        } else {
            distanceKm = 0f
            calories = 0f
            goalProgress = 0f
        }
        val statsLine = String.format(Locale.getDefault(), "%.2f km · %.0f kcal", distanceKm, calories)

        provideContent {
            val size = LocalSize.current
            val compact = size.height < 70.dp
            val ringDiameter = when {
                compact -> 40.dp
                size.width < 200.dp -> 76.dp
                else -> 116.dp
            }
            val density = LocalContext.current.resources.displayMetrics.density
            val ringBitmap = renderRingBitmap(
                diameterPx = (ringDiameter.value * density).toInt(),
                strokeWidthPx = ringDiameter.value * density * 0.14f,
                progress = goalProgress,
                ringColor = TextPrimary.toArgb(),
                trackColor = RingTrack.toArgb(),
            )

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(BackgroundDeep))
                    .cornerRadius(20.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (compact) {
                    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                        Image(provider = ImageProvider(ringBitmap), contentDescription = null, modifier = GlanceModifier.size(ringDiameter))
                        Spacer(GlanceModifier.width(8.dp))
                        Column {
                            Text("$steps", style = TextStyle(color = ColorProvider(TextPrimary), fontSize = 16.sp, fontWeight = FontWeight.Bold))
                            Text("steps", style = TextStyle(color = ColorProvider(TextMuted), fontSize = 10.sp))
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(provider = ImageProvider(ringBitmap), contentDescription = null, modifier = GlanceModifier.size(ringDiameter))
                            Text(
                                "$steps",
                                style = TextStyle(
                                    color = ColorProvider(TextPrimary),
                                    fontSize = stepsFontSize(ringDiameter),
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                        Spacer(GlanceModifier.height(6.dp))
                        Text(statsLine, style = TextStyle(color = ColorProvider(TextMuted), fontSize = 11.sp))
                    }
                }
            }
        }
    }
}

private fun stepsFontSize(ringDiameter: Dp) = if (ringDiameter > 90.dp) 26.sp else 18.sp
