package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.presentation.components.VuMeter
import kotlinx.coroutines.launch

@Composable
fun QueueScreen(
    facade: AudioPlayerFacade,
    libraryRepository: LibraryRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val playerStatus by facade.playerStatus.collectAsState()
    val activeZone by facade.activeZone.collectAsState()

    val isLocal = activeZone.isLocal || activeZone.isOffline || activeZone.isAndroidAuto

    // Reactive local queue flow
    val localQueue by facade.localQueue.collectAsState()

    // Remote queue state
    var remoteQueue by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoadingRemote by remember { mutableStateOf(false) }

    // Fetch remote queue when zone, active playing track count, or current index changes
    val trackCount = playerStatus?.playingNowTracks ?: 0
    val activeIndex = playerStatus?.playingNowPosition ?: -1
    val isPlaying = playerStatus?.state == PlaybackState.PLAYING

    LaunchedEffect(activeZone, trackCount, isLocal) {
        if (!isLocal) {
            isLoadingRemote = true
            scope.launch {
                try {
                    val tracks = libraryRepository.getRemoteQueue()
                    remoteQueue = tracks
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoadingRemote = false
                }
            }
        }
    }

    val currentQueue = if (isLocal) localQueue else remoteQueue

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bg1)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.text
                )
            }

            Text(
                text = "Play Queue".uppercase(),
                style = AppTypography.monoLabel,
                color = AppColors.accent
            )

            Button(
                onClick = {
                    facade.clearQueue()
                    if (!isLocal) {
                        remoteQueue = emptyList()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg2),
                border = BorderStroke(1.dp, AppColors.line),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "Clear".uppercase(),
                    style = AppTypography.monoLabel.copy(fontSize = 10.sp, color = AppColors.error)
                )
            }
        }

        if (isLoadingRemote && !isLocal) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.accent)
            }
        } else if (currentQueue.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "QUEUE IS EMPTY",
                    style = AppTypography.monoLabel,
                    color = AppColors.text3
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = currentQueue,
                    key = { index, track -> "${track.fileKey}_$index" }
                ) { index, track ->
                    val isActive = index == activeIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) AppColors.bg3 else AppColors.bg2)
                            .border(
                                width = 1.dp,
                                color = if (isActive) AppColors.accent else AppColors.line,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                facade.playByIndex(index)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Track index / Active Indicator
                        Box(
                            modifier = Modifier.width(36.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (isActive) {
                                VuMeter(isPlaying = isPlaying)
                            } else {
                                Text(
                                    text = String.format("%02d", index + 1),
                                    style = AppTypography.monoLabel,
                                    color = AppColors.text3
                                )
                            }
                        }

                        // Title and Artist
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = track.name,
                                style = AppTypography.itemTitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isActive) AppColors.accent else AppColors.text
                            )
                            Text(
                                text = track.artist,
                                style = AppTypography.itemSubtitle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = AppColors.text2
                            )
                        }

                        // Reorder controls (Up / Down)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (index > 0) {
                                        facade.moveQueueTrack(index, index - 1)
                                        if (!isLocal) {
                                            // Optimistic UI updates for remote reordering
                                            val mutableList = remoteQueue.toMutableList()
                                            val item = mutableList.removeAt(index)
                                            mutableList.add(index - 1, item)
                                            remoteQueue = mutableList
                                        }
                                    }
                                },
                                enabled = index > 0,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Move Up",
                                    tint = if (index > 0) AppColors.text else AppColors.line
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (index < currentQueue.size - 1) {
                                        facade.moveQueueTrack(index, index + 1)
                                        if (!isLocal) {
                                            // Optimistic UI updates for remote reordering
                                            val mutableList = remoteQueue.toMutableList()
                                            val item = mutableList.removeAt(index)
                                            mutableList.add(index + 1, item)
                                            remoteQueue = mutableList
                                        }
                                    }
                                },
                                enabled = index < currentQueue.size - 1,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Move Down",
                                    tint = if (index < currentQueue.size - 1) AppColors.text else AppColors.line
                                )
                            }

                            // Delete button
                            IconButton(
                                onClick = {
                                    facade.removeQueueTrack(index)
                                    if (!isLocal) {
                                        // Optimistic UI updates for remote deleting
                                        val mutableList = remoteQueue.toMutableList()
                                        mutableList.removeAt(index)
                                        remoteQueue = mutableList
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove from Queue",
                                    tint = AppColors.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
