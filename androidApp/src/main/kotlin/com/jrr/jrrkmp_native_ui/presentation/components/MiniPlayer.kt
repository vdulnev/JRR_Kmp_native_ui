package com.jrr.jrrkmp_native_ui.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography

@Composable
fun MiniPlayer(
    title: String,
    artist: String,
    imageUrl: String?,
    isPlaying: Boolean,
    progress: Float, // value between 0.0f and 1.0f
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onBodyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Outer Container: bg3 surface with line2 border and 12dp radius
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.55f),
                spotColor = Color.Black.copy(alpha = 0.55f)
            )
            .background(AppColors.bg3, shape = RoundedCornerShape(12.dp))
            .border(1.dp, AppColors.line2, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onBodyClick)
    ) {
        // 1. Flush Top-edge 2px gold progress indicator
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.TopStart)
        ) {
            val progressWidth = size.width * progress.coerceIn(0f, 1f)
            drawRect(
                color = AppColors.accent,
                size = Size(width = progressWidth, height = size.height)
            )
        }

        // 2. Mini-Player Body
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .padding(top = 2.dp), // Adjust for the top progress bar
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 38dp Artwork preview
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.bg2)
                    .border(1.dp, AppColors.line2, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val finalImageUrl = androidx.compose.runtime.remember(imageUrl) {
                        val fileParam = "File="
                        val index = imageUrl.indexOf(fileParam)
                        val fileKey = if (index != -1) {
                            val start = index + fileParam.length
                            val end = imageUrl.indexOf('&', start)
                            if (end == -1) imageUrl.substring(start) else imageUrl.substring(start, end)
                        } else null
                        
                        if (fileKey != null) {
                            val artFile = java.io.File(context.filesDir, "downloads/art_${fileKey}.jpg")
                            if (artFile.exists()) {
                                artFile
                            } else {
                                imageUrl
                            }
                        } else {
                            imageUrl
                        }
                    }
                    AsyncImage(
                        model = finalImageUrl,
                        contentDescription = "Artwork Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Custom fallback placeholder (gold/blue diagonal stripe style)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(color = Color(0xFF1E293B))
                        // Draw diagonal stripe in gold
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, size.height)
                        }
                        drawPath(
                            path = path,
                            color = AppColors.accent.copy(alpha = 0.5f),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Track metadata
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = AppTypography.itemTitle.copy(fontSize = 13.5.sp),
                    maxLines = 1
                )
                Text(
                    text = artist,
                    style = AppTypography.itemSubtitle.copy(fontSize = 11.5.sp, color = AppColors.text2),
                    maxLines = 1
                )
            }

            // Transport Row Controls: Prev -> Play Disc -> Next
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Previous Button (stroke width 1.5)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onPrevClick),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        // Vertical bar
                        val pathBar = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.2f)
                            lineTo(size.width * 0.2f, size.height * 0.8f)
                        }
                        drawPath(
                            path = pathBar,
                            color = AppColors.text2,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Left-pointing triangle outline
                        val pathTri = Path().apply {
                            moveTo(size.width * 0.8f, size.height * 0.2f)
                            lineTo(size.width * 0.35f, size.height * 0.5f)
                            lineTo(size.width * 0.8f, size.height * 0.8f)
                            close()
                        }
                        drawPath(
                            path = pathTri,
                            color = AppColors.text2,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Play / Pause gold disc (32dp)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            clip = false,
                            ambientColor = AppColors.accent.copy(alpha = 0.45f),
                            spotColor = AppColors.accent.copy(alpha = 0.45f)
                        )
                        .background(AppColors.accent, shape = CircleShape)
                        .clickable(onClick = onPlayPauseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(14.dp)) {
                        if (isPlaying) {
                            // Draw Pause vertical bars (filled)
                            val barWidth = size.width * 0.2f
                            drawRect(
                                color = AppColors.bg0,
                                topLeft = Offset(size.width * 0.25f, size.height * 0.2f),
                                size = Size(barWidth, size.height * 0.6f)
                            )
                            drawRect(
                                color = AppColors.bg0,
                                topLeft = Offset(size.width * 0.55f, size.height * 0.2f),
                                size = Size(barWidth, size.height * 0.6f)
                            )
                        } else {
                            // Draw Play triangle (filled)
                            val path = Path().apply {
                                moveTo(size.width * 0.3f, size.height * 0.2f)
                                lineTo(size.width * 0.8f, size.height * 0.5f)
                                lineTo(size.width * 0.3f, size.height * 0.8f)
                                close()
                            }
                            drawPath(path = path, color = AppColors.bg0)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Next Button (stroke width 1.5)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onNextClick),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        // Right-pointing triangle outline
                        val pathTri = Path().apply {
                            moveTo(size.width * 0.2f, size.height * 0.2f)
                            lineTo(size.width * 0.65f, size.height * 0.5f)
                            lineTo(size.width * 0.2f, size.height * 0.8f)
                            close()
                        }
                        drawPath(
                            path = pathTri,
                            color = AppColors.text2,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Vertical bar
                        val pathBar = Path().apply {
                            moveTo(size.width * 0.8f, size.height * 0.2f)
                            lineTo(size.width * 0.8f, size.height * 0.8f)
                        }
                        drawPath(
                            path = pathBar,
                            color = AppColors.text2,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
