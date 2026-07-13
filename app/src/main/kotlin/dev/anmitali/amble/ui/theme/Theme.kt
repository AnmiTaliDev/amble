// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AmbleColorScheme = darkColorScheme(
    primary = AccentColor,
    onPrimary = BackgroundDeep,
    secondary = AccentColor,
    background = BackgroundDeep,
    onBackground = TextPrimary,
    surface = SurfaceTile,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceTileElevated,
    onSurfaceVariant = TextMuted,
    outline = TextFaint,
)

@Composable
fun AmbleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AmbleColorScheme,
        typography = AmbleTypography,
        shapes = AmbleShapes,
        content = content,
    )
}
