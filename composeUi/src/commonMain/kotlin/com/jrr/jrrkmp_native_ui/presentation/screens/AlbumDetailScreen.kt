package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.presentation.components.InfoDialog
import com.jrr.jrrkmp_native_ui.presentation.components.toInfoFields
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.AlbumDetailContentState
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.AlbumDetailViewModel

private val DownloadIcon: ImageVector
    get() = Icons.Default.PlayArrow // Using PlayArrow as a visual placeholder for download arrow

@Composable
fun AlbumDetailScreen(
    viewModel: AlbumDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val platformUi = com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi.current
    var infoTrack by remember { mutableStateOf<Track?>(null) }
    var infoAlbum by remember { mutableStateOf<Album?>(null) }

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            platformUi.showToast(error)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.text)
            }

            Text(
                text = "Album".uppercase(),
                style = AppTypography.monoLabel,
                color = AppColors.accent
            )

            val isFav = (state.contentState as? AlbumDetailContentState.Success)?.isFavorite ?: false
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "Favorite",
                        tint = if (isFav) AppColors.accent else AppColors.text3
                    )
                }

                var showAlbumMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showAlbumMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Album Options",
                            tint = AppColors.text
                        )
                    }
                    DropdownMenu(
                        expanded = showAlbumMenu,
                        onDismissRequest = { showAlbumMenu = false },
                        modifier = Modifier.background(AppColors.bg2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Info", style = AppTypography.itemTitle) },
                            onClick = {
                                showAlbumMenu = false
                                infoAlbum = viewModel.album
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Play Album", style = AppTypography.itemTitle) },
                            onClick = {
                                showAlbumMenu = false
                                viewModel.playAlbum()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Play Next", style = AppTypography.itemTitle) },
                            onClick = {
                                showAlbumMenu = false
                                viewModel.playAlbumNext()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Queue", style = AppTypography.itemTitle) },
                            onClick = {
                                showAlbumMenu = false
                                viewModel.addAlbumToQueue()
                            }
                        )
                        if (!state.isOfflineMode) {
                            DropdownMenuItem(
                                text = { Text("Download Album", style = AppTypography.itemTitle) },
                                onClick = {
                                    showAlbumMenu = false
                                    viewModel.downloadAlbum()
                                }
                            )
                        }
                    }
                }
            }
        }

        when (val content = state.contentState) {
            is AlbumDetailContentState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${content.message}", color = AppColors.error, style = AppTypography.itemTitle)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            AlbumDetailContentState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            }
            is AlbumDetailContentState.Success -> {
                val tracks = content.tracks
                val downloadedTrackKeys = content.downloadedTrackKeys
                val favoritedTrackKeys = content.favoritedTrackKeys
                val activeJobs = content.activeDownloadJobs
                val artworkUrl = tracks.firstOrNull()?.let { LocalMcwsClient.current.buildImageUrl(it.fileKey) }

                if (isLarge) {
                    // Two columns: art + actions (left), tracklist (right).
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .width(360.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 36.dp, vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AlbumArtBlock(
                                album = viewModel.album,
                                tracks = tracks,
                                artworkUrl = artworkUrl,
                                isLarge = true,
                                onPlay = { viewModel.playAlbum() },
                                onShuffle = { viewModel.shuffleAlbum() }
                            )
                        }
                        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AppColors.line))
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            albumTrackItems(tracks, downloadedTrackKeys, favoritedTrackKeys, activeJobs, state.isOfflineMode, viewModel) { infoTrack = it }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AlbumArtBlock(
                                    album = viewModel.album,
                                    tracks = tracks,
                                    artworkUrl = artworkUrl,
                                    isLarge = false,
                                    onPlay = { viewModel.playAlbum() },
                                    onShuffle = { viewModel.shuffleAlbum() }
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }
                        albumTrackItems(tracks, downloadedTrackKeys, favoritedTrackKeys, activeJobs, state.isOfflineMode, viewModel) { infoTrack = it }
                    }
                }
            }
        }
    }

    infoTrack?.let { track ->
        InfoDialog(
            title = track.name,
            fields = track.toInfoFields(),
            onDismiss = { infoTrack = null }
        )
    }

    infoAlbum?.let { album ->
        InfoDialog(
            title = album.name,
            fields = album.toInfoFields(),
            onDismiss = { infoAlbum = null }
        )
    }
}

/**
 * Album artwork + metadata + PLAY/SHUFFLE actions. Shared by the phone (as the
 * tracklist header) and large-screen (as the left art column) layouts. The
 * large variant fills its column width and adds the year · tracks · duration
 * stat line.
 */
@Composable
private fun AlbumArtBlock(
    album: Album,
    tracks: List<Track>,
    artworkUrl: String?,
    isLarge: Boolean,
    onPlay: () -> Unit,
    onShuffle: () -> Unit
) {
    val artModifier = if (isLarge) {
        Modifier.fillMaxWidth().aspectRatio(1f)
    } else {
        Modifier.size(200.dp)
    }
    Box(
        modifier = artModifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bg2)
            .border(1.dp, AppColors.line2, RoundedCornerShape(8.dp))
    ) {
        if (!artworkUrl.isNullOrEmpty()) {
            AsyncImage(
                model = artworkUrl,
                contentDescription = album.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = album.name,
        style = AppTypography.screenTitle.copy(fontSize = 20.sp),
        maxLines = 2,
        modifier = Modifier.padding(horizontal = 8.dp)
    )

    Text(
        text = album.albumArtist,
        style = AppTypography.itemSubtitle.copy(color = AppColors.text2),
        maxLines = 1,
        modifier = Modifier.padding(top = 4.dp, bottom = if (isLarge) 12.dp else 16.dp)
    )

    if (isLarge) {
        val totalMin = (tracks.sumOf { it.durationMs } / 60000).toInt()
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (album.date.isNotBlank()) {
                Text(album.date, style = AppTypography.monoLabel.copy(color = AppColors.text3, fontSize = 10.sp))
            }
            Text("${tracks.size} TRACKS", style = AppTypography.monoLabel.copy(color = AppColors.text3, fontSize = 10.sp))
            Text("$totalMin MIN", style = AppTypography.monoLabel.copy(color = AppColors.text3, fontSize = 10.sp))
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Play / Shuffle Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onPlay,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent),
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = AppColors.bg0)
            Spacer(modifier = Modifier.width(8.dp))
            Text("PLAY", style = AppTypography.chipMono, color = AppColors.bg0)
        }

        Button(
            onClick = onShuffle,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg2),
            modifier = Modifier.weight(1f)
        ) {
            Text("🔀", style = AppTypography.chipMono, color = AppColors.text)
            Spacer(modifier = Modifier.width(8.dp))
            Text("SHUFFLE", style = AppTypography.chipMono, color = AppColors.text)
        }
    }
}

/**
 * Tracklist rows grouped by disc/side, with per-track download state and the
 * overflow menu. Shared by both album-detail layouts as a [LazyListScope]
 * extension so it slots into either LazyColumn unchanged.
 */
private fun LazyListScope.albumTrackItems(
    tracks: List<Track>,
    downloadedTrackKeys: Set<String>,
    favoritedTrackKeys: Set<String>,
    activeJobs: Map<String, String>,
    isOfflineMode: Boolean,
    viewModel: AlbumDetailViewModel,
    onInfoTrack: (Track) -> Unit
) {
    val discGroups = tracks.groupBy { it.discNumber }
    discGroups.forEach { (discNumber, discTracks) ->
        item {
            Text(
                text = (if (discGroups.size > 1) "DISC $discNumber" else "SIDE A").uppercase(),
                style = AppTypography.sectionLabel,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        itemsIndexed(discTracks) { idx, track ->
            val isDownloaded = downloadedTrackKeys.contains(track.fileKey)
            val jobState = activeJobs[track.fileKey]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.playTrack(track) }
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
                    val durationSec = track.durationMs / 1000
                    val timeStr = "${durationSec / 60}:${(durationSec % 60).toString().padStart(2, '0')}"
                    val subtitleText = if (track.artist != viewModel.album.albumArtist) {
                        "${track.artist} • $timeStr"
                    } else {
                        timeStr
                    }
                    Text(subtitleText, style = AppTypography.itemSubtitle, maxLines = 1)
                }

                if (favoritedTrackKeys.contains(track.fileKey)) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorited",
                        tint = AppColors.accent,
                        modifier = Modifier.size(16.dp).padding(horizontal = 2.dp)
                    )
                }

                if (track.numberPlays > 0) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "${track.numberPlays} plays",
                        tint = AppColors.text3,
                        modifier = Modifier.size(16.dp).padding(horizontal = 2.dp)
                    )
                }

                Box(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDownloaded) {
                        Icon(
                            imageVector = Icons.Default.Save,
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
                    }
                }

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
                            text = { Text("Info", style = AppTypography.itemTitle) },
                            onClick = {
                                showMenu = false
                                onInfoTrack(track)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Play", style = AppTypography.itemTitle) },
                            onClick = {
                                showMenu = false
                                viewModel.playTrack(track)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Play Next", style = AppTypography.itemTitle) },
                            onClick = {
                                showMenu = false
                                viewModel.playTrackNext(track)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Queue", style = AppTypography.itemTitle) },
                            onClick = {
                                showMenu = false
                                viewModel.addTrackToQueue(track)
                            }
                        )
                        val isFav = favoritedTrackKeys.contains(track.fileKey)
                        DropdownMenuItem(
                            text = { Text(if (isFav) "Remove from Favorites" else "Add to Favorites", style = AppTypography.itemTitle) },
                            onClick = {
                                showMenu = false
                                viewModel.toggleFavoriteTrack(track)
                            }
                        )
                        if (!isOfflineMode) {
                            DropdownMenuItem(
                                text = { Text("Download", style = AppTypography.itemTitle) },
                                onClick = {
                                    showMenu = false
                                    viewModel.startDownload(track)
                                }
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = AppColors.line, thickness = 0.5.dp)
        }
    }
}
