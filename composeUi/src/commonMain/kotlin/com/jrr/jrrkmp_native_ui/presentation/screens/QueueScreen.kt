package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrr.jrrkmp_native_ui.core.di.LocalPlayCounts
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.presentation.components.VuMeter
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.QueueViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(
    viewModel: QueueViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRail: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val platformUi = com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi.current

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            platformUi.showToast(error)
            viewModel.clearTransientError()
        }
    }

    val currentQueue = state.queueTracks
    val activeIndex = state.activeIndex
    val isPlaying = state.isPlaying
    val isLoading = state.isLoading

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
            if (isRail) {
                // Queue rail (large screen): no back button — the queue is
                // always visible beside Now Playing. Show count instead.
                Column {
                    Text(
                        text = "Play Queue".uppercase(),
                        style = AppTypography.sectionLabel,
                    )
                    Text(
                        text = "${currentQueue.size} ${if (currentQueue.size == 1) "TRACK" else "TRACKS"}",
                        style = AppTypography.monoLabel.copy(color = AppColors.text3, fontSize = 10.sp),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            } else {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.text
                    )
                }

                Text(
                    text = "Play Queue".uppercase(),
                    style = AppTypography.monoLabel,
                    color = AppColors.accent
                )
            }

            Button(
                onClick = { viewModel.clearQueue() },
                enabled = currentQueue.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg2, disabledContainerColor = AppColors.bg2.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, AppColors.line),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = "Clear".uppercase(),
                    style = AppTypography.monoLabel.copy(
                        fontSize = 10.sp,
                        color = if (currentQueue.isNotEmpty()) AppColors.error else AppColors.text3
                    )
                )
            }
        }

        if (isLoading) {
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
                    val currentTrackKey = track.fileKey

                    // confirmValueChange is deprecated without a drop-in
                    // replacement — the suggested rework is dynamic anchored-
                    // draggable anchors, which would restructure this whole
                    // swipe-to-remove row. Suppress until that rework.
                    @Suppress("DEPRECATION")
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.removeQueueTrack(index)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    LaunchedEffect(currentTrackKey) {
                        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                            dismissState.reset()
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val direction = dismissState.dismissDirection
                            val alignment = when (direction) {
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                else -> Alignment.Center
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.error.copy(alpha = 0.8f))
                                    .padding(horizontal = 20.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = AppColors.bg0
                                )
                            }
                        },
                        content = {
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
                                        viewModel.playByIndex(index)
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

                                val isDownloaded = state.downloadedTrackKeys.contains(track.fileKey)
                                val isFav = state.favoritedTrackKeys.contains(track.fileKey)

                                if (isFav) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Favorited",
                                        tint = AppColors.accent,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(horizontal = 2.dp)
                                    )
                                }

                                val plays = LocalPlayCounts.current[track.fileKey] ?: track.numberPlays
                                if (plays > 0) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = "$plays plays",
                                        tint = AppColors.text3,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(horizontal = 2.dp)
                                    )
                                }

                                if (isDownloaded) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Downloaded",
                                        tint = AppColors.accent,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(horizontal = 2.dp)
                                    )
                                }

                                // Track duration
                                val durationSec = track.durationMs / 1000
                                val timeStr = "${durationSec / 60}:${(durationSec % 60).toString().padStart(2, '0')}"
                                Text(
                                    text = timeStr,
                                    style = AppTypography.monoLabel,
                                    color = AppColors.text3,
                                    modifier = Modifier.padding(start = 8.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                var showMenu by remember { mutableStateOf(false) }
                                Box {
                                    IconButton(
                                        onClick = { showMenu = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More options",
                                            tint = AppColors.text3,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        modifier = Modifier.background(AppColors.bg2)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Play", style = AppTypography.itemTitle) },
                                            onClick = {
                                                showMenu = false
                                                viewModel.playByIndex(index)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(if (isFav) "Remove from Favorites" else "Add to Favorites", style = AppTypography.itemTitle) },
                                            onClick = {
                                                showMenu = false
                                                viewModel.toggleFavoriteTrack(track)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Remove from Queue", style = AppTypography.itemTitle) },
                                            onClick = {
                                                showMenu = false
                                                viewModel.removeQueueTrack(index)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
