package com.jrr.jrrkmp_native_ui.presentation.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode as ComposeRepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.NowPlayingViewModel
import java.util.Locale

@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearTransientError()
        }
    }

    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableStateOf(0f) }

    val isPlaying = state.isPlaying
    val currentPosition = state.positionMs
    val duration = state.durationMs

    val displayProgress = if (isScrubbing) {
        scrubProgress
    } else if (duration > 0) {
        currentPosition.toFloat() / duration.toFloat()
    } else {
        0f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bg1)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Now Playing".uppercase(),
                    style = AppTypography.monoLabel,
                    color = AppColors.accent
                )
                Text(
                    text = state.activeZoneName,
                    style = AppTypography.itemTitle.copy(fontSize = 14.sp),
                    color = AppColors.text2
                )
            }

            IconButton(onClick = onQueueClick) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "Queue",
                    tint = AppColors.text
                )
            }
        }

        // Vinyl Sleeve Component
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            VinylSleeve(
                albumTitle = state.albumTitle,
                artistName = state.artistName,
                year = "2026", // Fallback decorative year
                side = "SIDE A",
                imageUrl = null,
                isPlaying = isPlaying
            )
        }

        // Metadata block & technical format badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = state.trackTitle,
                style = AppTypography.screenTitle.copy(fontSize = 20.sp),
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = state.artistName + " — " + state.albumTitle,
                style = AppTypography.itemSubtitle.copy(color = AppColors.text2),
                maxLines = 1,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Technical details badge
            if (state.sampleRate > 0) {
                val formatString = buildString {
                    append("AUDIO")
                    append(" | ")
                    append("${state.sampleRate / 1000}kHz")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.bg2)
                        .border(1.dp, AppColors.line2, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = formatString.uppercase(),
                        style = AppTypography.monoLabel.copy(color = AppColors.text3, fontSize = 9.sp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar and times
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = displayProgress,
                onValueChange = {
                    isScrubbing = true
                    scrubProgress = it
                },
                onValueChangeFinished = {
                    isScrubbing = false
                    if (duration > 0) {
                        viewModel.seekTo((scrubProgress * duration).toLong())
                    }
                },
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.accent,
                    activeTrackColor = AppColors.accent,
                    inactiveTrackColor = AppColors.line2
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentSecs = if (isScrubbing) (scrubProgress * duration / 1000).toLong() else currentPosition / 1000
                val totalSecs = duration / 1000
                Text(
                    text = formatTime(currentSecs),
                    style = AppTypography.monoLabel,
                    color = AppColors.text2
                )
                Text(
                    text = formatTime(totalSecs),
                    style = AppTypography.monoLabel,
                    color = AppColors.text2
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Transport Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle
            IconButton(
                onClick = { viewModel.toggleShuffle() }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (state.shuffleMode != ShuffleMode.OFF) AppColors.accent else AppColors.text3,
                        modifier = Modifier.size(22.dp)
                    )
                    if (state.shuffleMode == ShuffleMode.AUTOMATIC) {
                        Text(
                            text = "AUTO",
                            style = AppTypography.monoLabel.copy(fontSize = 7.sp),
                            color = AppColors.accent
                        )
                    }
                }
            }

            // Previous
            IconButton(onClick = { viewModel.previous() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = AppColors.text,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Play/Pause circular disc
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(AppColors.accent)
                    .clickable {
                        if (isPlaying) viewModel.pause() else viewModel.play()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = AppColors.bg0,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Next
            IconButton(onClick = { viewModel.next() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = AppColors.text,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Repeat
            IconButton(
                onClick = { viewModel.toggleRepeat() }
            ) {
                Icon(
                    imageVector = if (state.repeatMode == RepeatMode.TRACK) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (state.repeatMode != RepeatMode.OFF) AppColors.accent else AppColors.text3,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Volume control row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (state.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = AppColors.text3,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Slider(
                value = state.volume,
                onValueChange = { viewModel.setVolume(it) },
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.text2,
                    activeTrackColor = AppColors.text2,
                    inactiveTrackColor = AppColors.line
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${(state.volume * 100).toInt()}",
                style = AppTypography.monoLabel,
                color = AppColors.text3,
                modifier = Modifier.width(28.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}

@Composable
fun VinylSleeve(
    albumTitle: String,
    artistName: String,
    year: String,
    side: String,
    imageUrl: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            rotation.animateTo(
                targetValue = rotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 8000, easing = LinearEasing),
                    repeatMode = ComposeRepeatMode.Restart
                )
            )
        } else {
            rotation.stop()
        }
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        // Outer Vinyl record sticking out of the sleeve slightly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp)
                .rotate(rotation.value)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color.Black)
                .border(2.dp, androidx.compose.ui.graphics.Color(0xFF1C1C1C), CircleShape)
        ) {
            // Vinyl details lines (grooves)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
                    .border(1.dp, androidx.compose.ui.graphics.Color(0xFF151515), CircleShape)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(64.dp)
                    .border(1.dp, androidx.compose.ui.graphics.Color(0xFF151515), CircleShape)
            )

            // Center Label
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(AppColors.bg3)
                    .border(1.dp, AppColors.line2, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Spindle hole
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AppColors.bg0)
                )
            }
        }

        // Cardboard Sleeve (covers the vinyl on the left)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bg2)
                .border(1.dp, AppColors.line2, RoundedCornerShape(8.dp))
                .padding(20.dp)
        ) {
            // Sleeve design layout (Minimalist vintage look)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top label metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = side.uppercase(),
                        style = AppTypography.monoLabel.copy(fontSize = 9.sp),
                        color = AppColors.accent
                    )
                    Text(
                        text = year,
                        style = AppTypography.monoLabel.copy(fontSize = 9.sp),
                        color = AppColors.text3
                    )
                }

                // Album Artwork or Large Lettering in center of sleeve
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.bg3)
                        .border(0.5.dp, AppColors.line, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = albumTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Large letter monogram representation
                        Text(
                            text = albumTitle.take(1).uppercase(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.accentDim,
                            style = AppTypography.screenTitle
                        )
                    }
                }

                // Bottom Titles
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = albumTitle,
                        style = AppTypography.itemTitle.copy(fontSize = 15.sp),
                        maxLines = 1,
                        color = AppColors.text
                    )
                    Text(
                        text = artistName.uppercase(),
                        style = AppTypography.monoLabel.copy(fontSize = 9.sp, letterSpacing = 1.sp),
                        maxLines = 1,
                        color = AppColors.text2,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.US, "%d:%02d", m, s)
}
