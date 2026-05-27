package com.jrr.jrrkmp_native_ui.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.AlbumDetailViewModel

private val DownloadIcon: ImageVector
    get() = Icons.Default.PlayArrow // Using PlayArrow as a visual placeholder for download arrow

@Composable
fun AlbumDetailScreen(
    viewModel: AlbumDetailViewModel,
    onBackClick: () -> Unit,
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bg1)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.text)
            }

            Text(
                text = "Album".uppercase(),
                style = AppTypography.monoLabel,
                color = AppColors.accent
            )

            val isFav = state.isFavorite
            IconButton(onClick = { viewModel.toggleFavorite() }) {
                Icon(
                    imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = if (isFav) AppColors.accent else AppColors.text3
                )
            }
        }

        val errorMessage = state.errorMessage
        when {
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: $errorMessage", color = AppColors.error, style = AppTypography.itemTitle)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            }
            else -> {
                val tracks = state.tracks
                val downloadedTrackKeys = state.downloadedTrackKeys
                val activeJobs = state.activeDownloadJobs

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header (Artwork and Details)
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.bg2)
                                    .border(1.dp, AppColors.line2, RoundedCornerShape(8.dp))
                            ) {
                                val artworkUrl = tracks.firstOrNull()?.let { LocalMcwsClient.current.buildImageUrl(it.fileKey) }
                                if (!artworkUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = artworkUrl,
                                        contentDescription = viewModel.album.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = viewModel.album.name,
                                style = AppTypography.screenTitle.copy(fontSize = 20.sp),
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Text(
                                text = viewModel.album.albumArtist,
                                style = AppTypography.itemSubtitle.copy(color = AppColors.text2),
                                maxLines = 1,
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )

                            // Play / Shuffle Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.playAlbum() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AppColors.bg0)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("PLAY", style = AppTypography.chipMono, color = AppColors.bg0)
                                }

                                Button(
                                    onClick = { viewModel.shuffleAlbum() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg2),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🔀", style = AppTypography.chipMono, color = AppColors.text)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("SHUFFLE", style = AppTypography.chipMono, color = AppColors.text)
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Render tracklist with side/disc headers
                    val discGroups = tracks.groupBy { it.discNumber }
                    discGroups.forEach { (discNumber, discTracks) ->
                        if (discGroups.size > 1) {
                            item {
                                Text(
                                    text = "DISC $discNumber".uppercase(),
                                    style = AppTypography.sectionLabel,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                        } else {
                            item {
                                Text(
                                    text = "SIDE A".uppercase(),
                                    style = AppTypography.sectionLabel,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                        }

                        itemsIndexed(discTracks) { idx, track ->
                            val isDownloaded = downloadedTrackKeys.contains(track.fileKey)
                            val jobState = activeJobs[track.fileKey]

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.playTrack(track)
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = String.format("%02d", if (track.trackNumber == 0) idx + 1 else track.trackNumber),
                                    style = AppTypography.monoLabel.copy(color = AppColors.accent),
                                    modifier = Modifier.width(36.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.name, style = AppTypography.itemTitle, maxLines = 1)
                                    if (track.artist != viewModel.album.albumArtist) {
                                        Text(track.artist, style = AppTypography.itemSubtitle, maxLines = 1)
                                    }
                                }

                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDownloaded) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Downloaded",
                                            tint = AppColors.accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else if (jobState != null) {
                                        CircularProgressIndicator(
                                            color = AppColors.accent,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(
                                            onClick = { viewModel.startDownload(track) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = DownloadIcon,
                                                contentDescription = "Download",
                                                tint = AppColors.text3,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                val durationSec = track.durationMs / 1000
                                Text(
                                    text = String.format(java.util.Locale.US, "%d:%02d", durationSec / 60, durationSec % 60),
                                    style = AppTypography.monoLabel
                                )
                            }
                            HorizontalDivider(color = AppColors.line, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
