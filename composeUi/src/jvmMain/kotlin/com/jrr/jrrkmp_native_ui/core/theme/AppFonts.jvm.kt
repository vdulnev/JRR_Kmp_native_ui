package com.jrr.jrrkmp_native_ui.core.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

actual object AppFonts {
    actual val Inter: FontFamily = FontFamily(
        Font("font/inter_regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("font/inter_medium.ttf", FontWeight.Medium, FontStyle.Normal),
        Font("font/inter_semibold.ttf", FontWeight.SemiBold, FontStyle.Normal),
        Font("font/inter_bold.ttf", FontWeight.Bold, FontStyle.Normal),
    )

    actual val IbmPlexMono: FontFamily = FontFamily(
        Font("font/ibm_plex_mono_regular.ttf", FontWeight.Normal, FontStyle.Normal),
        Font("font/ibm_plex_mono_medium.ttf", FontWeight.Medium, FontStyle.Normal),
    )
}
