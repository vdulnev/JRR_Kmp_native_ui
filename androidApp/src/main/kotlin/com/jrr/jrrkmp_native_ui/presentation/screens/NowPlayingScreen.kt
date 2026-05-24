package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.presentation.components.VinylSleeve
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    facade: AudioPlayerFacade,
    onQueueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerStatus by facade.playerStatus.collectAsState()
    val activeZone by facade.activeZone.collectAsState()

    val status = playerStatus

    // Local state for scrubbing
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableStateOf(0f) }

    val isPlaying = status?.state == PlaybackState.PLAYING
    val currentPosition = status?.positionMs ?: 0L
    val duration = status?.durationMs ?: 0L

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
                    text = activeZone.name,
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
                albumTitle = status?.trackAlbum?.ifEmpty { "No Track" } ?: "No Track",
                artistName = status?.trackArtist?.ifEmpty { "Unknown Artist" } ?: "Unknown Artist",
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
                text = status?.trackName?.ifEmpty { "Idle" } ?: "Idle",
                style = AppTypography.screenTitle.copy(fontSize = 20.sp),
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = (status?.trackArtist?.ifEmpty { "Unknown Artist" } ?: "Unknown Artist") + " — " + (status?.trackAlbum?.ifEmpty { "Unknown Album" } ?: "Unknown Album"),
                style = AppTypography.itemSubtitle.copy(color = AppColors.text2),
                maxLines = 1,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Technical details badge
            if (status != null && status.sampleRate > 0) {
                val formatString = buildString {
                    append("AUDIO")
                    append(" | ")
                    append("${status.sampleRate / 1000}kHz")
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
                        facade.seekTo((scrubProgress * duration).toLong())
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
                onClick = {
                    val currentMode = status?.shuffleMode ?: ShuffleMode.OFF
                    val nextMode = when (currentMode) {
                        ShuffleMode.OFF -> ShuffleMode.ON
                        ShuffleMode.ON -> ShuffleMode.AUTOMATIC
                        ShuffleMode.AUTOMATIC -> ShuffleMode.OFF
                    }
                    facade.setShuffleMode(nextMode)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (status?.shuffleMode != ShuffleMode.OFF) AppColors.accent else AppColors.text3,
                        modifier = Modifier.size(22.dp)
                    )
                    if (status?.shuffleMode == ShuffleMode.AUTOMATIC) {
                        Text(
                            text = "AUTO",
                            style = AppTypography.monoLabel.copy(fontSize = 7.sp),
                            color = AppColors.accent
                        )
                    }
                }
            }

            // Previous
            IconButton(onClick = { facade.previous() }) {
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
                        if (isPlaying) facade.pause() else facade.play()
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
            IconButton(onClick = { facade.next() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = AppColors.text,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Repeat
            IconButton(
                onClick = {
                    val currentMode = status?.repeatMode ?: RepeatMode.OFF
                    val nextMode = when (currentMode) {
                        RepeatMode.OFF -> RepeatMode.PLAYLIST
                        RepeatMode.PLAYLIST -> RepeatMode.TRACK
                        RepeatMode.TRACK -> RepeatMode.OFF
                    }
                    facade.setRepeatMode(nextMode)
                }
            ) {
                Icon(
                    imageVector = if (status?.repeatMode == RepeatMode.TRACK) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (status?.repeatMode != RepeatMode.OFF) AppColors.accent else AppColors.text3,
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
                imageVector = if (status?.isMuted == true) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = AppColors.text3,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Slider(
                value = status?.volume ?: 0.5f,
                onValueChange = { facade.setVolume(it) },
                colors = SliderDefaults.colors(
                    thumbColor = AppColors.text2,
                    activeTrackColor = AppColors.text2,
                    inactiveTrackColor = AppColors.line
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "${((status?.volume ?: 0.5f) * 100).toInt()}",
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
    return String.format(Locale.US, "%d:%02d", m, s)
}
