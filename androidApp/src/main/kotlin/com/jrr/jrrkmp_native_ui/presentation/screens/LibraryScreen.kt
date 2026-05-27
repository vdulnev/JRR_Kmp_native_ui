package com.jrr.jrrkmp_native_ui.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreVert
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.outlinedTextFieldColors
import com.jrr.jrrkmp_native_ui.data.api.BrowseItem
import com.jrr.jrrkmp_native_ui.data.api.BrowseNode
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onAlbumClick: (Album) -> Unit, // Album Name, Artist Name
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearTransientError()
        }
    }

    LaunchedEffect(Unit) {
        if (viewModel.state.value.artists.isEmpty() && !viewModel.state.value.isLoading) {
            viewModel.retry()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bg1)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSearching) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = {
                        viewModel.updateSearchQuery(it)
                    },
                    placeholder = { Text("Search tracks, artists...", color = AppColors.text3) },
                    singleLine = true,
                    colors = outlinedTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = AppColors.text3) },
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearching = false
                            viewModel.updateSearchQuery("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Search", tint = AppColors.text)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = "Library".uppercase(),
                    style = AppTypography.screenTitle,
                    color = AppColors.text
                )

                IconButton(onClick = { isSearching = true }) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = AppColors.text)
                }
            }
        }

        if (isSearching) {
            // Render Search Results
            if (state.isTabLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.searchResults) { track ->
                        TrackRowItem(
                            track = track,
                            onPlay = { viewModel.playTrack(track) },
                            onPlayNext = { viewModel.playTrackNext(track) },
                            onAddToQueue = { viewModel.addTrackToQueue(track) },
                            onDownload = { viewModel.downloadTrack(track) },
                            isOffline = state.isOffline,
                            onClick = {
                                viewModel.playTrack(track)
                            }
                        )
                    }
                }
            }
        } else {
            val tabs = if (state.isOffline) {
                listOf("Artists" to "artists", "Downloads" to "downloads", "Favorites" to "favorites")
            } else {
                listOf("Artists" to "artists", "Random" to "random", "Browse" to "browse", "Downloads" to "downloads", "Favorites" to "favorites")
            }

            val selectedIndex = tabs.indexOfFirst { it.second == state.currentTab }.coerceAtLeast(0)

            // Tabs Row
            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = AppColors.accent,
                divider = {}
            ) {
                tabs.forEach { (label, tabId) ->
                    Tab(
                        selected = state.currentTab == tabId,
                        onClick = { viewModel.switchTab(tabId) },
                        text = {
                            Text(
                                label.uppercase(),
                                style = AppTypography.chipMono,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.currentTab) {
                    "artists" -> ArtistsTab(
                        artists = state.artists,
                        selectedArtist = state.selectedArtist,
                        artistAlbums = state.artistAlbums,
                        isLoadingArtists = state.isLoading,
                        isLoadingAlbums = state.isTabLoading,
                        onArtistClick = { artistName ->
                            viewModel.selectArtist(artistName)
                        },
                        onAlbumClick = onAlbumClick,
                        onPlayAlbum = { viewModel.playAlbum(it) },
                        onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                        onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                        onDownloadAlbum = { viewModel.downloadAlbum(it) },
                        isOffline = state.isOffline,
                        onBackClick = { viewModel.selectArtist(null) }
                    )
                    "random" -> RandomTab(
                        albums = state.randomAlbums,
                        isLoading = state.isLoading,
                        onAlbumClick = onAlbumClick,
                        onPlayAlbum = { viewModel.playAlbum(it) },
                        onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                        onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                        onDownloadAlbum = { viewModel.downloadAlbum(it) },
                        isOffline = state.isOffline,
                        onRefresh = {
                            viewModel.retry()
                        }
                    )
                    "browse" -> BrowseTab(
                        stack = state.browseStack,
                        children = state.browseChildren,
                        tracks = state.browseTracks,
                        isLoading = state.isLoading || state.isTabLoading,
                        onNodeClick = { label, id ->
                            viewModel.pushBrowseNode(label, id)
                        },
                        onTrackClick = { clickedTrack, allTracks ->
                            val startIndex = allTracks.indexOf(clickedTrack).coerceAtLeast(0)
                            viewModel.playTracks(allTracks, startIndex)
                        },
                        onPlayTrack = { viewModel.playTrack(it) },
                        onPlayTrackNext = { viewModel.playTrackNext(it) },
                        onAddTrackToQueue = { viewModel.addTrackToQueue(it) },
                        onDownloadTrack = { viewModel.downloadTrack(it) },
                        onPlayBrowseItem = { viewModel.playBrowseItem(it) },
                        onPlayBrowseItemNext = { viewModel.playBrowseItemNext(it) },
                        onAddBrowseItemToQueue = { viewModel.addBrowseItemToQueue(it) },
                        onDownloadBrowseItem = { viewModel.downloadBrowseItem(it) },
                        isOffline = state.isOffline,
                        onBackClick = {
                            viewModel.popBrowseNode()
                        }
                    )
                    "downloads" -> DownloadsTab(
                        tracks = state.downloadedTracks,
                        isLoading = state.isLoading,
                        onTrackClick = { clickedTrack, allTracks ->
                            val startIndex = allTracks.indexOf(clickedTrack).coerceAtLeast(0)
                            viewModel.playTracks(allTracks, startIndex)
                        }
                    )
                    "favorites" -> FavoritesTab(
                        onAlbumClick = onAlbumClick,
                        onPlayAlbum = { viewModel.playAlbum(it) },
                        onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                        onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                        onDownloadAlbum = { viewModel.downloadAlbum(it) },
                        isOffline = state.isOffline
                    )
                }
            }
        }
    }
}

@Composable
fun ArtistsTab(
    artists: List<String>,
    selectedArtist: String?,
    artistAlbums: List<Album>,
    isLoadingArtists: Boolean,
    isLoadingAlbums: Boolean,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayAlbumNext: (Album) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
    onDownloadAlbum: (Album) -> Unit,
    isOffline: Boolean,
    onBackClick: () -> Unit
) {
    if (selectedArtist != null) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBackClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.accent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedArtist, style = AppTypography.subScreenTitle)
            }

            if (isLoadingAlbums) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(artistAlbums) { album ->
                        AlbumRowItem(
                            album = album,
                            onPlay = { onPlayAlbum(album) },
                            onPlayNext = { onPlayAlbumNext(album) },
                            onAddToQueue = { onAddAlbumToQueue(album) },
                            onDownload = { onDownloadAlbum(album) },
                            isOffline = isOffline,
                            onClick = { onAlbumClick(album) }
                        )
                    }
                }
            }
        }
    } else {
        if (isLoadingArtists) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.accent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(artists) { artist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.bg2)
                            .clickable { onArtistClick(artist) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppColors.accentDim),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = artist.take(1).uppercase(),
                                style = AppTypography.chipMono.copy(color = AppColors.accent, fontSize = 14.sp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(artist, style = AppTypography.itemTitle)
                    }
                }
            }
        }
    }
}

@Composable
fun RandomTab(
    albums: List<Album>,
    isLoading: Boolean,
    onAlbumClick: (Album) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayAlbumNext: (Album) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
    onDownloadAlbum: (Album) -> Unit,
    isOffline: Boolean,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Suggested Albums".uppercase(), style = AppTypography.sectionLabel)
            Button(
                onClick = onRefresh,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Text("Refresh", style = AppTypography.chipMono.copy(fontSize = 10.sp, color = AppColors.accent))
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.accent)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(albums) { album ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAlbumClick(album) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.bg2)
                                .border(1.dp, AppColors.line2, RoundedCornerShape(8.dp))
                        ) {
                            val imageUrl = LocalMcwsClient.current.buildImageUrl(album.artworkFileKey)
                            if (imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = album.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(album.name, style = AppTypography.itemTitle, maxLines = 1)
                                Text(album.albumArtist, style = AppTypography.itemSubtitle, maxLines = 1)
                            }

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
                                            onPlayAlbum(album)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Play Next", style = AppTypography.itemTitle) },
                                        onClick = {
                                            showMenu = false
                                            onPlayAlbumNext(album)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Add to Queue", style = AppTypography.itemTitle) },
                                        onClick = {
                                            showMenu = false
                                            onAddAlbumToQueue(album)
                                        }
                                    )
                                    if (!isOffline) {
                                        DropdownMenuItem(
                                            text = { Text("Download", style = AppTypography.itemTitle) },
                                            onClick = {
                                                showMenu = false
                                                onDownloadAlbum(album)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrowseTab(
    stack: List<BrowseNode>,
    children: List<BrowseItem>,
    tracks: List<Track>,
    isLoading: Boolean,
    onNodeClick: (String, String) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayTrack: (Track) -> Unit,
    onPlayTrackNext: (Track) -> Unit,
    onAddTrackToQueue: (Track) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onPlayBrowseItem: (BrowseItem) -> Unit,
    onPlayBrowseItemNext: (BrowseItem) -> Unit,
    onAddBrowseItemToQueue: (BrowseItem) -> Unit,
    onDownloadBrowseItem: (BrowseItem) -> Unit,
    isOffline: Boolean,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Breadcrumbs / Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (stack.size > 1) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.accent)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = stack.last().label.uppercase(),
                style = AppTypography.sectionLabel,
                maxLines = 1
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.accent)
            }
        } else {
            if (children.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    children.forEach { (nodeId, nodeLabel) ->
                        item {
                            val browseItem = BrowseItem(nodeId, nodeLabel)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.bg2)
                                    .clickable { onNodeClick(nodeLabel, nodeId) }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(nodeLabel, style = AppTypography.itemTitle, modifier = Modifier.weight(1f))

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
                                                onPlayBrowseItem(browseItem)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Play Next", style = AppTypography.itemTitle) },
                                            onClick = {
                                                showMenu = false
                                                onPlayBrowseItemNext(browseItem)
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Add to Queue", style = AppTypography.itemTitle) },
                                            onClick = {
                                                showMenu = false
                                                onAddBrowseItemToQueue(browseItem)
                                            }
                                        )
                                        if (!isOffline) {
                                            DropdownMenuItem(
                                                text = { Text("Download", style = AppTypography.itemTitle) },
                                                onClick = {
                                                    showMenu = false
                                                    onDownloadBrowseItem(browseItem)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Render leaf node tracks
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tracks) { track ->
                        TrackRowItem(
                            track = track,
                            onPlay = { onPlayTrack(track) },
                            onPlayNext = { onPlayTrackNext(track) },
                            onAddToQueue = { onAddTrackToQueue(track) },
                            onDownload = { onDownloadTrack(track) },
                            isOffline = isOffline,
                            onClick = { onTrackClick(track, tracks) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadsTab(
    tracks: List<Track>,
    isLoading: Boolean,
    onTrackClick: (Track, List<Track>) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.accent)
        }
    } else {
        if (tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No downloaded tracks", style = AppTypography.itemTitle, color = AppColors.text3)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tracks) { track ->
                    TrackRowItem(
                        track = track,
                        onClick = {
                            onTrackClick(track, tracks)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesTab(
    onAlbumClick: (Album) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayAlbumNext: (Album) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
    onDownloadAlbum: (Album) -> Unit,
    isOffline: Boolean
) {
    val context = LocalContext.current
    val database = remember { context.appContainer.database }
    var favorites by remember { mutableStateOf<List<com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity>>(emptyList()) }

    LaunchedEffect(Unit) {
        favorites = database.favoriteDao().getAllFavorites()
    }

    val favoritedAlbums = favorites.filter { it.type == "album" }

    if (favoritedAlbums.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, contentDescription = "Favorites", tint = AppColors.accent, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your Favorites", style = AppTypography.itemTitle)
                Text("Pinned albums will appear here", style = AppTypography.itemSubtitle, color = AppColors.text3)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favoritedAlbums) { fav ->
                val parts = fav.identifier.split("|")
                val albumName = parts.getOrNull(0) ?: fav.displayName
                val artist = parts.getOrNull(1) ?: "Unknown Artist"
                val album = Album(
                    name = albumName,
                    albumArtist = artist,
                    folderPath = "",
                    parentFolderPath = "",
                    date = "",
                    artworkFileKey = "",
                    totalDiscs = 1,
                    discNumber = 1
                )
                AlbumRowItem(
                    album = album,
                    onPlay = { onPlayAlbum(album) },
                    onPlayNext = { onPlayAlbumNext(album) },
                    onAddToQueue = { onAddAlbumToQueue(album) },
                    onDownload = { onDownloadAlbum(album) },
                    isOffline = isOffline,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: Track,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    isOffline: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bg2)
            .border(1.dp, AppColors.line, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.bg3)
        ) {
            val imageUrl = LocalMcwsClient.current.buildImageUrl(track.fileKey)
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = track.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(track.name, style = AppTypography.itemTitle, maxLines = 1)
            Text(track.artist, style = AppTypography.itemSubtitle, color = AppColors.text2, maxLines = 1)
        }

        Spacer(modifier = Modifier.width(8.dp))

        val durationSec = track.durationMs / 1000
        Text(
            text = String.format(java.util.Locale.US, "%d:%02d", durationSec / 60, durationSec % 60),
            style = AppTypography.monoLabel
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
                        onPlay()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Play Next", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        onPlayNext()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Queue", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        onAddToQueue()
                    }
                )
                if (!isOffline) {
                    DropdownMenuItem(
                        text = { Text("Download", style = AppTypography.itemTitle) },
                        onClick = {
                            showMenu = false
                            onDownload()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumRowItem(
    album: Album,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit,
    isOffline: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bg2)
            .border(1.dp, AppColors.line, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.bg3)
        ) {
            val imageUrl = LocalMcwsClient.current.buildImageUrl(album.artworkFileKey)
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = album.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(album.name, style = AppTypography.itemTitle, maxLines = 1)
            Text(album.date.ifEmpty { "Unknown Year" }, style = AppTypography.itemSubtitle, color = AppColors.text2)
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
                    text = { Text("Play", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        onPlay()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Play Next", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        onPlayNext()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Queue", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        onAddToQueue()
                    }
                )
                if (!isOffline) {
                    DropdownMenuItem(
                        text = { Text("Download", style = AppTypography.itemTitle) },
                        onClick = {
                            showMenu = false
                            onDownload()
                        }
                    )
                }
            }
        }
    }
}
