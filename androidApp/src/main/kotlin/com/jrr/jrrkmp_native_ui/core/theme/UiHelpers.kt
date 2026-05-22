package com.jrr.jrrkmp_native_ui.core.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.accent,
    unfocusedBorderColor = AppColors.line2,
    focusedLabelColor = AppColors.accent,
    unfocusedLabelColor = AppColors.text3,
    focusedTextColor = AppColors.text,
    unfocusedTextColor = AppColors.text,
    cursorColor = AppColors.accent
)

@Composable
fun BoxBorder(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
