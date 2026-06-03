package com.jrr.jrrkmp_native_ui.core.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTypography {
    val screenTitle = TextStyle(
        fontFamily = AppFonts.Inter,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
        color = AppColors.text
    )

    val subScreenTitle = TextStyle(
        fontFamily = AppFonts.Inter,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
        color = AppColors.text
    )

    val nowPlayingTitle = TextStyle(
        fontFamily = AppFonts.Inter,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
        color = AppColors.text
    )

    val itemTitle = TextStyle(
        fontFamily = AppFonts.Inter,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.2).sp,
        color = AppColors.text
    )

    val itemSubtitle = TextStyle(
        fontFamily = AppFonts.Inter,
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
        color = AppColors.text2
    )

    val labelLarge = TextStyle(
        fontFamily = AppFonts.Inter,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
        color = AppColors.text
    )

    val sectionLabel = TextStyle(
        fontFamily = AppFonts.IbmPlexMono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 2.5.sp,
        color = AppColors.accent
    )

    val sectionHeading = TextStyle(
        fontFamily = AppFonts.IbmPlexMono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 1.6.sp,
        color = AppColors.text3
    )

    val monoLabel = TextStyle(
        fontFamily = AppFonts.IbmPlexMono,
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.2.sp,
        color = AppColors.text3,
        fontFeatureSettings = "tnum",
        lineHeight = 13.2.sp // line-height: 1.2 to keep mono baseline level
    )

    val chipMono = TextStyle(
        fontFamily = AppFonts.IbmPlexMono,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.6.sp,
        color = AppColors.text
    )
}
