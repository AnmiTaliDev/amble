// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.anmitali.amble.ui.theme.AccentColor
import dev.anmitali.amble.ui.theme.SurfaceTileElevated
import dev.anmitali.amble.ui.theme.TextFaint

@Composable
fun AmbleButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = SurfaceTileElevated,
        border = BorderStroke(1.dp, AccentColor.copy(alpha = if (enabled) 0.55f else 0.15f)),
    ) {
        Box(modifier = Modifier.padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (enabled) AccentColor else TextFaint,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
