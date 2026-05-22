package com.jrr.jrrkmp_native_ui.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors

@Composable
fun VuMeter(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "VuMeterTransition")

    // Bouncing scale factors for the 5 bars
    val p1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )
    val p2 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )
    val p3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p3"
    )
    val p4 by transition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p4"
    )
    val p5 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p5"
    )

    // Base heights: 7dp, 11dp, 5dp, 13dp, 8dp
    val baseHeights = listOf(7.dp, 11.dp, 5.dp, 13.dp, 8.dp)

    // Total width is 5 bars * 2dp + 4 gaps * 2dp = 18dp
    // Canvas height is max height 13dp + 1dp buffer = 14dp
    Canvas(
        modifier = modifier.size(width = 18.dp, height = 14.dp)
    ) {
        val barWidthPx = 2.dp.toPx()
        val gapPx = 2.dp.toPx()
        val maxCanvasHeight = size.height

        for (i in 0 until 5) {
            val baseHeightPx = baseHeights[i].toPx()
            val barHeightPx = if (isPlaying) {
                val scale = when (i) {
                    0 -> p1
                    1 -> p2
                    2 -> p3
                    3 -> p4
                    else -> p5
                }
                baseHeightPx * scale
            } else {
                // Return a flat idle state (2dp) when paused
                2.dp.toPx()
            }

            val xOffset = i * (barWidthPx + gapPx)
            val yOffset = maxCanvasHeight - barHeightPx

            drawRoundRect(
                color = AppColors.accent,
                topLeft = Offset(x = xOffset, y = yOffset),
                size = Size(width = barWidthPx, height = barHeightPx),
                cornerRadius = CornerRadius(x = 1.dp.toPx(), y = 1.dp.toPx())
            )
        }
    }
}
