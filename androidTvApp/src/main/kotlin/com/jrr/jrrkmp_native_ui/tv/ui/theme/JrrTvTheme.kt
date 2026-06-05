@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

/**
 * App-wide dark theme for the TV UI. Without an explicit [colorScheme],
 * tv-material3 falls back to its light defaults, which look wrong over the black
 * window. This applies the JRR brand palette (gold accent on near-black).
 */
@Composable
fun JrrTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = JrrGold,
            onPrimary = JrrDark,
            primaryContainer = JrrGold,
            onPrimaryContainer = JrrDark,
            secondary = JrrGold,
            onSecondary = JrrDark,
            background = JrrDark,
            onBackground = JrrOnSurface,
            surface = JrrDark,
            onSurface = JrrOnSurface,
            surfaceVariant = JrrSurface,
            onSurfaceVariant = JrrMuted,
            border = JrrMuted,
            error = JrrError,
            onError = JrrDark,
        ),
        content = content,
    )
}
