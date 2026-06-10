package com.jrr.jrrkmp_native_ui.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography

@Composable
fun VinylSleeve(
    albumTitle: String,
    artistName: String,
    year: String,
    side: String = "SIDE A",
    imageUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    // Outer size 260dp x 260dp
    Box(
        modifier = modifier
            .size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Underglow: 220dp x 220dp radial gold gradient, blurred
        Box(
            modifier = Modifier
                .size(220.dp)
                .blur(32.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AppColors.accent.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(110.dp)
                )
        )

        // Wrapper Box that applies the 3D perspective transforms
        // transform: rotateX(8) rotateY(-14) rotateZ(-3)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationX = 8f
                    rotationY = -14f
                    rotationZ = -3f
                    cameraDistance = 12f * density
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // 2. LP record disc: 248dp x 248dp, offset 64dp right of sleeve when playing
            val discOffset by animateDpAsState(
                targetValue = if (isPlaying) 64.dp else 0.dp,
                animationSpec = tween(durationMillis = 500),
                label = "DiscOffsetAnimation"
            )

            Box(
                modifier = Modifier
                    .size(248.dp)
                    .offset(x = discOffset)
                    .align(Alignment.CenterStart)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val center = Offset(x = canvasWidth / 2f, y = canvasHeight / 2f)
                    val outerRadius = canvasWidth / 2f

                    // Draw the black matte vinyl disc body
                    drawCircle(
                        color = Color(0xFF0F0F11),
                        radius = outerRadius
                    )

                    // Draw concentric vinyl grooves (concentric circles)
                    var grooveRadius = outerRadius - 12.dp.toPx()
                    val minGrooveRadius = 42.dp.toPx()
                    val grooveSpacing = 6.dp.toPx()

                    while (grooveRadius > minGrooveRadius) {
                        drawCircle(
                            color = Color(0xFF1E1E21),
                            radius = grooveRadius,
                            style = Stroke(width = 1.dp.toPx())
                        )
                        grooveRadius -= grooveSpacing
                    }

                    // Draw the center 72dp gold label disc (diameter = 72dp -> radius = 36dp)
                    drawCircle(
                        color = AppColors.accent,
                        radius = 36.dp.toPx()
                    )

                    // Draw center hole 6dp (radius = 3dp) in deepest background color bg0
                    drawCircle(
                        color = AppColors.bg0,
                        radius = 3.dp.toPx()
                    )
                }
            }

            // 3. Sleeve: 256dp x 256dp, 4dp radius, dark gradient + repeating 135° gold stripe
            Box(
                modifier = Modifier
                    .size(256.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(AppColors.bg2, AppColors.bg0)
                        )
                    )
            ) {
                // Background album artwork if available
                if (!imageUrl.isNullOrEmpty()) {
                    val resolver = com.jrr.jrrkmp_native_ui.presentation.LocalArtworkResolver.current
                    val finalImageUrl = androidx.compose.runtime.remember(imageUrl, resolver) {
                        resolver.resolve(imageUrl)
                    }
                    AsyncImage(
                        model = finalImageUrl,
                        contentDescription = "Album Artwork",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Sleeve decoration (stripes & inset border)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw 135-degree repeating gold stripe at 6% opacity
                    val step = 20.dp.toPx()
                    val lineCount = (size.width + size.height) / step
                    for (i in 0..lineCount.toInt()) {
                        val offset = i * step
                        drawLine(
                            color = AppColors.accent.copy(alpha = 0.06f),
                            start = Offset(x = offset, y = 0f),
                            end = Offset(x = offset - size.height, y = size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // 1-px inset border at 18px (which is 6dp)
                    val inset = 6.dp.toPx()
                    drawRect(
                        color = AppColors.accentSoft,
                        topLeft = Offset(x = inset, y = inset),
                        size = Size(
                            width = size.width - 2 * inset,
                            height = size.height - 2 * inset
                        ),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Overlaid text content inside the sleeve
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp) // Sits exactly inside the inset border line
                ) {
                    // Top-Left metadata: "SIDE A · YEAR"
                    Text(
                        text = "$side · $year".uppercase(),
                        style = AppTypography.monoLabel.copy(
                            color = AppColors.accent
                        ),
                        modifier = Modifier.align(Alignment.TopStart)
                    )

                    // Bottom-Left Titles
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Text(
                            text = albumTitle.uppercase(),
                            style = AppTypography.screenTitle,
                            maxLines = 2
                        )
                        Text(
                            text = artistName.uppercase(),
                            style = AppTypography.sectionHeading.copy(
                                color = AppColors.text2
                            ),
                            maxLines = 1,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
