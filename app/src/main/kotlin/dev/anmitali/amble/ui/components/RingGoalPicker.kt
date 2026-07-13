// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.anmitali.amble.ui.theme.AccentColor
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
fun RingGoalPicker(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    step: Float,
    valueText: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 200.dp,
    strokeWidth: Dp = 16.dp,
    label: String = "",
) {
    val progress = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)

    RingGauge(
        progress = progress,
        diameter = diameter,
        strokeWidth = strokeWidth,
        accentColor = AccentColor,
        modifier = modifier.pointerInput(valueRange, step) {
            fun updateFromOffset(offset: Offset) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val dx = offset.x - center.x
                val dy = offset.y - center.y
                val rawDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                val fraction = (((rawDeg + 90f) % 360f + 360f) % 360f) / 360f
                val rawValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                val stepped = (rawValue / step).roundToInt() * step
                onValueChange(stepped.coerceIn(valueRange.start, valueRange.endInclusive))
            }

            detectDragGestures(
                onDragStart = { updateFromOffset(it) },
                onDrag = { change, _ -> updateFromOffset(change.position) },
            )
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = valueText,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
