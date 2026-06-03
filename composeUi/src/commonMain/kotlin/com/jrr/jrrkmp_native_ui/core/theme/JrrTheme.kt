package com.jrr.jrrkmp_native_ui.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColors = staticCompositionLocalOf { AppColors }
val LocalSpacing = staticCompositionLocalOf { AppSpacing }
val LocalTypography = staticCompositionLocalOf { AppTypography }

@Composable
fun JrrTheme(
    content: @Composable () -> Unit
) {
    val darkColorScheme = darkColorScheme(
        primary = AppColors.accent,
        background = AppColors.bg1,
        surface = AppColors.bg2,
        onBackground = AppColors.text,
        onSurface = AppColors.text,
        error = AppColors.error
    )

    CompositionLocalProvider(
        LocalColors provides AppColors,
        LocalSpacing provides AppSpacing,
        LocalTypography provides AppTypography
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme,
            content = content
        )
    }
}
