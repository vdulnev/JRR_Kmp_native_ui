package com.jrr.jrrkmp_native_ui.core.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.jrr.jrrkmp_native_ui.R

object AppFonts {
    val Inter = FontFamily(
        Font(resId = R.font.inter_regular, weight = FontWeight.Normal),
        Font(resId = R.font.inter_medium, weight = FontWeight.Medium),
        Font(resId = R.font.inter_semibold, weight = FontWeight.SemiBold),
        Font(resId = R.font.inter_bold, weight = FontWeight.Bold)
    )

    val IbmPlexMono = FontFamily(
        Font(resId = R.font.ibm_plex_mono_regular, weight = FontWeight.Normal),
        Font(resId = R.font.ibm_plex_mono_medium, weight = FontWeight.Medium)
    )
}
