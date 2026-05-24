package com.jrr.jrrkmp_native_ui.presentation.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.outlinedTextFieldColors
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    facade: AudioPlayerFacade,
    libraryRepository: LibraryRepository,
    onAlbumClick: (String, String) -> Unit, // Album Name, Artist Name
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val activeZone by facade.activeZone.collectAsState()
    val isOffline = activeZone.isOffline

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<Track>>(emptyList()) }
    var searchJobActive by remember { mutableStateOf(false) }

    var currentTab by remember { mutableStateOf("artists") }

    // Tab content states
    var artists by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedArtist by remember { mutableStateOf<String?>(null) }
    var artistAlbums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var isLoadingArtistAlbums by remember { mutableStateOf(false) }

    var randomAlbums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var isLoadingRandom by remember { mutableStateOf(false) }

    // Browse tree stack: List of Pair(Node Label, Node ID)
    var browseStack by remember { mutableStateOf(listOf(Pair("Library", "-1"))) }
    var browseChildren by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var browseTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoadingBrowse by remember { mutableStateOf(false) }

    // Reset tab and reload artists when offline mode toggles
    LaunchedEffect(isOffline) {
        if (isOffline && (currentTab == "random" || currentTab == "browse")) {
            currentTab = "artists"
        }
        // Force refresh artists list to match offline/online mode state
        artists = libraryRepository.getArtists()
    }

    // Load initial tab data
    LaunchedEffect(currentTab) {
        when (currentTab) {
            "artists" -> {
                if (artists.isEmpty()) {
                    artists = libraryRepository.getArtists()
                }
            }
            "random" -> {
                if (randomAlbums.isEmpty()) {
                    isLoadingRandom = true
                    randomAlbums = libraryRepository.getRandomAlbums(20)
                    isLoadingRandom = false
                }
            }
            "browse" -> {
                // Load browse level
                val currentNode = browseStack.last()
                isLoadingBrowse = true
                val children = libraryRepository.getBrowseChildren(currentNode.second)
                if (children.isNotEmpty()) {
                    browseChildren = children
                    browseTracks = emptyList()
                } else {
                    // Leaf node, fetch tracks
                    browseChildren = emptyMap()
                    browseTracks = libraryRepository.getBrowseFiles(currentNode.second)
                }
                isLoadingBrowse = false
            }
            "favorites" -> {
                // Fetch favorite tracks or albums from DB if implemented
            }
        }
    }

    // Search query action
    val executeSearch = { query: String ->
        if (query.trim().isNotEmpty()) {
            searchJobActive = true
            scope.launch {
                searchResults = libraryRepository.searchFiles(query)
                searchJobActive = false
            }
        } else {
            searchResults = emptyList()
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
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        executeSearch(it)
                    },
                    placeholder = { Text("Search tracks, artists...", color = AppColors.text3) },
                    singleLine = true,
                    colors = outlinedTextFieldColors(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = AppColors.text3) },
                    trailingIcon = {
                        IconButton(onClick = {
                            isSearching = false
                            searchQuery = ""
                            searchResults = emptyList()
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
            if (searchJobActive) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { track ->
                        TrackRowItem(
                            track = track,
                            onClick = {
                                facade.setQueue(listOf(track), 0)
                            }
                        )
                    }
                }
            }
        } else {
            val tabs = if (isOffline) {
                listOf("Artists" to "artists", "Favorites" to "favorites")
            } else {
                listOf("Artists" to "artists", "Random" to "random", "Browse" to "browse", "Favorites" to "favorites")
            }

            val selectedIndex = tabs.indexOfFirst { it.second == currentTab }.coerceAtLeast(0)

            // Tabs Row
            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = AppColors.accent,
                divider = {}
            ) {
                tabs.forEach { (label, tabId) ->
                    Tab(
                        selected = currentTab == tabId,
                        onClick = { currentTab = tabId },
                        text = { Text(label.uppercase(), style = AppTypography.chipMono) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentTab) {
                    "artists" -> ArtistsTab(
                        artists = artists,
                        selectedArtist = selectedArtist,
                        artistAlbums = artistAlbums,
                        isLoadingAlbums = isLoadingArtistAlbums,
                        onArtistClick = { artistName ->
                            selectedArtist = artistName
                            isLoadingArtistAlbums = true
                            scope.launch {
                                artistAlbums = libraryRepository.getAlbumsByArtist(artistName)
                                isLoadingArtistAlbums = false
                            }
                        },
                        onAlbumClick = onAlbumClick,
                        onBackClick = { selectedArtist = null }
                    )
                    "random" -> RandomTab(
                        albums = randomAlbums,
                        isLoading = isLoadingRandom,
                        onAlbumClick = onAlbumClick,
                        onRefresh = {
                            isLoadingRandom = true
                            scope.launch {
                                randomAlbums = libraryRepository.getRandomAlbums(20)
                                isLoadingRandom = false
                            }
                        }
                    )
                    "browse" -> BrowseTab(
                        stack = browseStack,
                        children = browseChildren,
                        tracks = browseTracks,
                        isLoading = isLoadingBrowse,
                        onNodeClick = { label, id ->
                            val newStack = browseStack + Pair(label, id)
                            browseStack = newStack
                            isLoadingBrowse = true
                            scope.launch {
                                val ch = libraryRepository.getBrowseChildren(id)
                                if (ch.isNotEmpty()) {
                                    browseChildren = ch
                                    browseTracks = emptyList()
                                } else {
                                    browseChildren = emptyMap()
                                    browseTracks = libraryRepository.getBrowseFiles(id)
                                }
                                isLoadingBrowse = false
                            }
                        },
                        onTrackClick = { clickedTrack, allTracks ->
                            val startIndex = allTracks.indexOf(clickedTrack).coerceAtLeast(0)
                            facade.setQueue(allTracks, startIndex)
                        },
                        onBackClick = {
                            if (browseStack.size > 1) {
                                val newStack = browseStack.dropLast(1)
                                browseStack = newStack
                                val lastNode = newStack.last()
                                isLoadingBrowse = true
                                scope.launch {
                                    val ch = libraryRepository.getBrowseChildren(lastNode.second)
                                    if (ch.isNotEmpty()) {
                                        browseChildren = ch
                                        browseTracks = emptyList()
                                    } else {
                                        browseChildren = emptyMap()
                                        browseTracks = libraryRepository.getBrowseFiles(lastNode.second)
                                    }
                                    isLoadingBrowse = false
                                }
                            }
                        }
                    )
                    "favorites" -> FavoritesTab(onAlbumClick = onAlbumClick)
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
    isLoadingAlbums: Boolean,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String, String) -> Unit,
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
                        AlbumRowItem(album = album, onClick = { onAlbumClick(album.name, album.albumArtist) })
                    }
                }
            }
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

@Composable
fun RandomTab(
    albums: List<Album>,
    isLoading: Boolean,
    onAlbumClick: (String, String) -> Unit,
    onRefresh: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "REFRESH".uppercase(),
                style = AppTypography.chipMono.copy(color = AppColors.accent),
                modifier = Modifier.clickable(onClick = onRefresh)
            )
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
                            .clickable { onAlbumClick(album.name, album.albumArtist) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppColors.bg2)
                                .border(1.dp, AppColors.line2, RoundedCornerShape(4.dp))
                        ) {
                            val imageUrl = McwsClient.buildImageUrl(album.artworkFileKey)
                            if (imageUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = album.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(album.name, style = AppTypography.itemTitle, maxLines = 1)
                        Text(album.albumArtist, style = AppTypography.itemSubtitle, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun BrowseTab(
    stack: List<Pair<String, String>>,
    children: Map<String, String>,
    tracks: List<Track>,
    isLoading: Boolean,
    onNodeClick: (String, String) -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Breadcrumbs header
        if (stack.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBackClick)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.accent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stack.joinToString(" / ") { it.first },
                    style = AppTypography.itemSubtitle,
                    maxLines = 1
                )
            }
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
                    items(children.toList()) { (nodeLabel, nodeId) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.bg2)
                                .clickable { onNodeClick(nodeLabel, nodeId) }
                                .padding(16.dp)
                        ) {
                            Text(nodeLabel, style = AppTypography.itemTitle)
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
                        TrackRowItem(track = track, onClick = { onTrackClick(track, tracks) })
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesTab(
    onAlbumClick: (String, String) -> Unit
) {
    // Basic mock / display for favorites node
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, contentDescription = "Favorites", tint = AppColors.accent, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your Favorites", style = AppTypography.itemTitle)
            Text("Pinned albums & artists will appear here", style = AppTypography.itemSubtitle, color = AppColors.text3)
        }
    }
}

@Composable
fun TrackRowItem(track: Track, onClick: () -> Unit) {
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
            if (track.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = track.imageUrl,
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
    }
}

@Composable
fun AlbumRowItem(album: Album, onClick: () -> Unit) {
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
            val imageUrl = McwsClient.buildImageUrl(album.artworkFileKey)
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
    }
}
