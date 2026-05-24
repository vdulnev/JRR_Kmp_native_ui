package com.jrr.jrrkmp_native_ui.presentation.screens

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
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.launch

private val DownloadIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Download",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = androidx.compose.ui.graphics.SolidColor(androidx.compose.ui.graphics.Color.White),
            pathBuilder = {
                moveTo(19f, 9f)
                horizontalLineTo(15f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                verticalLineTo(9f)
                horizontalLineTo(5f)
                lineTo(12f, 16f)
                lineTo(19f, 9f)
                close()
                moveTo(5f, 18f)
                verticalLineTo(20f)
                horizontalLineTo(19f)
                verticalLineTo(18f)
                horizontalLineTo(5f)
                close()
            }
        )
    }.build()
}

@Composable
fun AlbumDetailScreen(
    albumName: String,
    artistName: String,
    facade: AudioPlayerFacade,
    libraryRepository: LibraryRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = remember { com.jrr.jrrkmp_native_ui.JrrDependencies.getDatabase(context) }
    val downloadedTracks by db.downloadedTrackDao().getAllTracksFlow().collectAsState(initial = emptyList())
    val downloadJobs by db.downloadJobDao().getAllJobsFlow().collectAsState(initial = emptyList())

    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isFavorite by remember { mutableStateOf(false) }

    // Fetch album tracks and check favorite state
    LaunchedEffect(albumName, artistName) {
        isLoading = true
        tracks = libraryRepository.getAlbumTracks(albumName, artistName)
        isLoading = false
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

            IconButton(onClick = { isFavorite = !isFavorite }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) AppColors.accent else AppColors.text3
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.accent)
            }
        } else {
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
                        // 2D artwork
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.bg2)
                                .border(1.dp, AppColors.line2, RoundedCornerShape(8.dp))
                        ) {
                            val artworkUrl = tracks.firstOrNull()?.imageUrl
                            if (!artworkUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = artworkUrl,
                                    contentDescription = albumName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = albumName,
                            style = AppTypography.screenTitle.copy(fontSize = 20.sp),
                            maxLines = 2,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Text(
                            text = artistName,
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
                                onClick = {
                                    if (tracks.isNotEmpty()) {
                                        facade.setQueue(tracks, 0)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AppColors.bg0)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PLAY", style = AppTypography.chipMono, color = AppColors.bg0)
                            }

                            Button(
                                onClick = {
                                    if (tracks.isNotEmpty()) {
                                        val shuffled = tracks.shuffled()
                                        facade.setQueue(shuffled, 0)
                                    }
                                },
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
                        val isDownloaded = downloadedTracks.any { it.fileKey == track.fileKey }
                        val job = downloadJobs.find { it.fileKey == track.fileKey }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val startIndex = tracks.indexOf(track).coerceAtLeast(0)
                                    facade.setQueue(tracks, startIndex)
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format("%02d", track.trackNumber.ifZero(idx + 1)),
                                style = AppTypography.monoLabel.copy(color = AppColors.accent),
                                modifier = Modifier.width(36.dp)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(track.name, style = AppTypography.itemTitle, maxLines = 1)
                                if (track.artist != artistName) {
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
                                } else if (job != null) {
                                    if (job.state == "DOWNLOADING" && job.bytesTotal > 0) {
                                        val progress = job.bytesDownloaded.toFloat() / job.bytesTotal.toFloat()
                                        CircularProgressIndicator(
                                            progress = progress,
                                            color = AppColors.accent,
                                            trackColor = AppColors.line,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            color = AppColors.accent,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                libraryRepository.startDownload(track)
                                            }
                                        },
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

fun Int.ifZero(value: Int): Int = if (this == 0) value else this
