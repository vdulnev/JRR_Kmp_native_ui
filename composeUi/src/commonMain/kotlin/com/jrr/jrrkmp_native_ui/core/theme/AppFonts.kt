package com.jrr.jrrkmp_native_ui.core.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * App font families. Declared `expect` so each platform supplies the
 * [FontFamily] from its own resource pipeline, without the (composable)
 * Compose Resources `Font()` API. That keeps [AppTypography] a plain static
 * object and leaves its ~186 call sites untouched.
 *
 *   - Android: font resources under res/font in this module's android source set
 *   - Desktop: classpath font resources under jvmMain/resources/font
 */
expect object AppFonts {
    val Inter: FontFamily
    val IbmPlexMono: FontFamily
}
