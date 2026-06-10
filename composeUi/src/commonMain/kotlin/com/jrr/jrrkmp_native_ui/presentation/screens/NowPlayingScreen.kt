package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.presentation.components.VinylSleeve
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.NowPlayingViewModel

@Composable
fun NowPlayingScreen(
    viewModel: NowPlayingViewModel,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier,
    showQueueButton: Boolean = true
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val platformUi = com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi.current

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            platformUi.showToast(error)
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

            if (showQueueButton) {
                IconButton(onClick = onQueueClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Queue",
                        tint = AppColors.text
                    )
                }
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
                imageUrl = state.imageUrl.ifEmpty { null },
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
                imageVector = if (state.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
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


fun formatTime(seconds: Long): String {
    val m = seconds / 60
    val s = seconds % 60
    return "$m:${s.toString().padStart(2, '0')}"
}
