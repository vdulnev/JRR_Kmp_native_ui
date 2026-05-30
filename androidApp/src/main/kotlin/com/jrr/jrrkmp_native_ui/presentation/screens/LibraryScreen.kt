package com.jrr.jrrkmp_native_ui.presentation.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Headphones
import com.jrr.jrrkmp_native_ui.presentation.components.InfoDialog
import com.jrr.jrrkmp_native_ui.presentation.components.toInfoFields
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
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
import com.jrr.jrrkmp_native_ui.presentation.components.AlphabetIndexBar
import com.jrr.jrrkmp_native_ui.presentation.components.sectionLetterFor
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

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
    var infoTrack by remember { mutableStateOf<Track?>(null) }
    var infoAlbum by remember { mutableStateOf<Album?>(null) }

    val artistsListState = rememberLazyListState()
    val artistAlbumsListState = rememberLazyListState()
    val compilationArtistsListState = rememberLazyListState()
    val randomAlbumsGridState = rememberLazyGridState()

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
                            onInfoClick = { infoTrack = track },
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
            SecondaryTabRow(
                selectedTabIndex = selectedIndex,
                containerColor = Color.Transparent,
                contentColor = AppColors.accent,
                indicator = {
                    if (selectedIndex < tabs.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(selectedIndex)
                        )
                    }
                },
                divider = {},
                tabs = {
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
                },
            )

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
                        compilationMode = state.compilationMode,
                        compilationArtists = state.compilationArtists,
                        isLoadingArtists = state.isLoading || state.isTabLoading,
                        isLoadingAlbums = state.isLoading || state.isTabLoading,
                        artistsListState = artistsListState,
                        artistAlbumsListState = artistAlbumsListState,
                        compilationArtistsListState = compilationArtistsListState,
                        onArtistClick = { viewModel.selectArtist(it) },
                        onCompilationArtistClick = { viewModel.selectCompilationArtist(it) },
                        onAlbumClick = onAlbumClick,
                        onPlayAlbum = { viewModel.playAlbum(it) },
                        onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                        onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                        onDownloadAlbum = { viewModel.downloadAlbum(it) },
                        isOffline = state.isOffline,
                        onAlbumInfoClick = { infoAlbum = it },
                        onBackClick = { viewModel.selectArtist(null) }
                    )
                    "random" -> RandomTab(
                        albums = state.randomAlbums,
                        isLoading = state.isLoading,
                        gridState = randomAlbumsGridState,
                        onAlbumClick = onAlbumClick,
                        onPlayAlbum = { viewModel.playAlbum(it) },
                        onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                        onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                        onDownloadAlbum = { viewModel.downloadAlbum(it) },
                        isOffline = state.isOffline,
                        onAlbumInfoClick = { infoAlbum = it },
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
                        onTrackInfoClick = { infoTrack = it },
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
                        },
                        onPlayTracks = { viewModel.playTracks(it, 0) },
                        onPlayTracksShuffled = { viewModel.playTracksShuffled(it) },
                        onPlayTracksNext = { viewModel.playTracksNext(it) },
                        onAddTracksToQueue = { viewModel.addTracksToQueue(it) },
                        onTrackInfoClick = { infoTrack = it },
                        onAlbumInfoClick = { infoAlbum = it }
                    )
                    "favorites" -> FavoritesTab(
                        onAlbumClick = onAlbumClick,
                        onPlayAlbum = { viewModel.playAlbum(it) },
                        onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                        onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                        onDownloadAlbum = { viewModel.downloadAlbum(it) },
                        isOffline = state.isOffline,
                        onAlbumInfoClick = { infoAlbum = it }
                    )
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


@Composable
fun ArtistsTab(
    artists: List<String>,
    selectedArtist: String?,
    artistAlbums: List<Album>,
    compilationMode: Boolean,
    compilationArtists: List<String>,
    isLoadingArtists: Boolean,
    isLoadingAlbums: Boolean,
    artistsListState: LazyListState,
    artistAlbumsListState: LazyListState,
    compilationArtistsListState: LazyListState,
    onArtistClick: (String) -> Unit,
    onCompilationArtistClick: (String?) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayAlbumNext: (Album) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
    onDownloadAlbum: (Album) -> Unit,
    isOffline: Boolean,
    onAlbumInfoClick: (Album) -> Unit,
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
                val albumsScope = rememberCoroutineScope()
                val albumSections = remember(artistAlbums) {
                    artistAlbums.map { sectionLetterFor(it.name) }
                }
                val albumLetters = remember(albumSections) { albumSections.distinct() }
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = artistAlbumsListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 28.dp, top = 16.dp, bottom = 16.dp
                        ),
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
                                onInfoClick = { onAlbumInfoClick(album) },
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    }
                    AlphabetIndexBar(
                        letters = albumLetters,
                        onLetterSelected = { letter ->
                            val idx = albumSections.indexOf(letter)
                            if (idx >= 0) albumsScope.launch {
                                artistAlbumsListState.scrollToItem(idx)
                            }
                        },
                    )
                }
            }
        }
    } else if (compilationMode) {
        // Compilations drill-down: "All" + the artists that appear inside
        // compilation albums. Picking one shows the matching compilations.
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
                Text("Compilations", style = AppTypography.subScreenTitle)
            }
            if (isLoadingArtists) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else {
                val compScope = rememberCoroutineScope()
                val compSections = remember(compilationArtists) {
                    compilationArtists.map { sectionLetterFor(it) }
                }
                val compLetters = remember(compSections) { compSections.distinct() }
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = compilationArtistsListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 28.dp, top = 8.dp, bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            CompilationArtistRow(
                                label = "All",
                                avatarText = "∗",
                                highlighted = true,
                                onClick = { onCompilationArtistClick(null) }
                            )
                        }
                        items(compilationArtists) { artist ->
                            CompilationArtistRow(
                                label = artist,
                                avatarText = artist.take(1).uppercase(),
                                highlighted = false,
                                onClick = { onCompilationArtistClick(artist) }
                            )
                        }
                    }
                    AlphabetIndexBar(
                        letters = compLetters,
                        onLetterSelected = { letter ->
                            val idx = compSections.indexOf(letter)
                            // +1 to skip the leading "All" row.
                            if (idx >= 0) compScope.launch {
                                compilationArtistsListState.scrollToItem(idx + 1)
                            }
                        },
                    )
                }
            }
        }
    } else {
        if (isLoadingArtists) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.accent)
            }
        } else {
            val scope = rememberCoroutineScope()
            val sections = remember(artists) { artists.map { sectionLetterFor(it) } }
            val letters = remember(sections) { sections.distinct() }
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    state = artistsListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 28.dp, top = 16.dp, bottom = 16.dp
                    ),
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
                AlphabetIndexBar(
                    letters = letters,
                    onLetterSelected = { letter ->
                        val idx = sections.indexOf(letter)
                        if (idx >= 0) scope.launch { artistsListState.scrollToItem(idx) }
                    },
                )
            }
        }
    }
}

@Composable
private fun CompilationArtistRow(
    label: String,
    avatarText: String,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bg2)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (highlighted) AppColors.accent else AppColors.accentDim),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarText,
                style = AppTypography.chipMono.copy(
                    color = if (highlighted) AppColors.bg0 else AppColors.accent,
                    fontSize = 14.sp
                )
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            style = if (highlighted) {
                AppTypography.itemTitle.copy(color = AppColors.accent)
            } else {
                AppTypography.itemTitle
            }
        )
    }
}

@Composable
fun RandomTab(
    albums: List<Album>,
    isLoading: Boolean,
    gridState: LazyGridState,
    onAlbumClick: (Album) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayAlbumNext: (Album) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
    onDownloadAlbum: (Album) -> Unit,
    isOffline: Boolean,
    onAlbumInfoClick: (Album) -> Unit,
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
                state = gridState,
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
                                        imageVector = Icons.Default.MoreHoriz,
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
                                            onAlbumInfoClick(album)
                                        }
                                    )
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
    onTrackInfoClick: (Track) -> Unit,
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
                            onInfoClick = { onTrackInfoClick(track) },
                            onClick = { onTrackClick(track, tracks) }
                        )
                    }
                }
            }
        }
    }
}

data class DownloadAlbum(
    val groupId: String,
    val name: String,
    val artworkFileKey: String,
    val trackCount: Int
)

@Composable
fun DownloadsTab(
    tracks: List<Track>,
    isLoading: Boolean,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayTracks: (List<Track>) -> Unit,
    onPlayTracksShuffled: (List<Track>) -> Unit,
    onPlayTracksNext: (List<Track>) -> Unit,
    onAddTracksToQueue: (List<Track>) -> Unit,
    onTrackInfoClick: (Track) -> Unit,
    onAlbumInfoClick: (Album) -> Unit
) {
    var selectedArtist by remember { mutableStateOf<String?>(null) }
    var selectedAlbumGroupId by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = selectedArtist != null) {
        if (selectedAlbumGroupId != null) {
            selectedAlbumGroupId = null
        } else {
            selectedArtist = null
        }
    }

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
            if (selectedArtist != null) {
                val artistTracks = remember(tracks, selectedArtist) {
                    tracks.filter {
                        val artist = it.albumArtist.ifEmpty { "Unknown Artist" }
                        artist.equals(selectedArtist, ignoreCase = true)
                    }
                }

                if (selectedAlbumGroupId != null) {
                    // Screen 3: Tracks for the selected album
                    val albumTracks = remember(artistTracks, selectedAlbumGroupId) {
                        artistTracks.filter { it.albumGroupId == selectedAlbumGroupId }
                            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
                    }
                    val firstTrack = albumTracks.firstOrNull()

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAlbumGroupId = null }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.accent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(firstTrack?.album ?: "Unknown Album", style = AppTypography.subScreenTitle)
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (firstTrack != null) {
                                item {
                                    AlbumHeaderItem(
                                        albumName = firstTrack.album,
                                        artistName = firstTrack.albumArtist,
                                        artworkFileKey = firstTrack.fileKey
                                    )
                                }
                            }
                            itemsIndexed(
                                items = albumTracks,
                                key = { _, track -> track.fileKey }
                            ) { idx, track ->
                                GroupedTrackRowItem(
                                    track = track,
                                    indexInAlbum = idx,
                                    onClick = { onTrackClick(track, albumTracks) },
                                    onPlayTracks = onPlayTracks,
                                    onPlayTracksShuffled = onPlayTracksShuffled,
                                    onPlayTracksNext = onPlayTracksNext,
                                    onAddTracksToQueue = onAddTracksToQueue,
                                    onInfoClick = { onTrackInfoClick(track) }
                                )
                            }
                        }
                    }
                } else {
                    // Screen 2: Albums for the selected artist
                    val albums = remember(artistTracks) {
                        artistTracks.groupBy { it.albumGroupId }
                            .map { (groupId, albumTracks) ->
                                val firstTrack = albumTracks.firstOrNull()
                                val albumName = firstTrack?.album ?: "Unknown Album"
                                val artworkFileKey = firstTrack?.fileKey ?: ""
                                DownloadAlbum(
                                    groupId = groupId,
                                    name = albumName,
                                    artworkFileKey = artworkFileKey,
                                    trackCount = albumTracks.size
                                )
                            }
                            .sortedWith(compareBy { it.name.lowercase() })
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedArtist = null }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.accent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(selectedArtist ?: "", style = AppTypography.subScreenTitle)
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(albums) { album ->
                                val albumTracks = remember(artistTracks, album.groupId) {
                                    artistTracks.filter { it.albumGroupId == album.groupId }
                                }
                                val firstTrack = remember(albumTracks) { albumTracks.firstOrNull() }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.bg2)
                                        .border(1.dp, AppColors.line, RoundedCornerShape(8.dp))
                                        .clickable { selectedAlbumGroupId = album.groupId }
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
                                        Text("${album.trackCount} ${if (album.trackCount == 1) "track" else "tracks"}", style = AppTypography.itemSubtitle, color = AppColors.text2)
                                    }

                                    val albumObj = remember(firstTrack) {
                                        firstTrack?.let { Album(it) }
                                    }
                                    TrackActionMenu(
                                        onPlay = { onPlayTracks(albumTracks) },
                                        onPlayShuffle = { onPlayTracksShuffled(albumTracks) },
                                        onPlayNext = { onPlayTracksNext(albumTracks) },
                                        onAddToQueue = { onAddTracksToQueue(albumTracks) },
                                        onInfoClick = albumObj?.let { { onAlbumInfoClick(it) } },
                                        icon = Icons.Default.MoreHoriz
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Screen 1: List of Artists
                val artists = remember(tracks) {
                    tracks.map { it.albumArtist.ifEmpty { "Unknown Artist" } }
                        .distinct()
                        .sortedWith(compareBy { it.lowercase() })
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All Downloads" header/special item
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.bg2)
                                .clickable { onPlayTracks(tracks) }
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
                                    text = "↓",
                                    style = AppTypography.chipMono.copy(color = AppColors.accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("All Downloads", style = AppTypography.itemTitle)
                                Text("${tracks.size} ${if (tracks.size == 1) "track" else "tracks"}", style = AppTypography.itemSubtitle, color = AppColors.text2)
                            }

                            TrackActionMenu(
                                onPlay = { onPlayTracks(tracks) },
                                onPlayShuffle = { onPlayTracksShuffled(tracks) },
                                onPlayNext = { onPlayTracksNext(tracks) },
                                onAddToQueue = { onAddTracksToQueue(tracks) }
                            )
                        }
                    }

                    items(artists) { artist ->
                        val artistTracks = remember(tracks, artist) {
                            tracks.filter {
                                val a = it.albumArtist.ifEmpty { "Unknown Artist" }
                                a.equals(artist, ignoreCase = true)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.bg2)
                                .clickable { selectedArtist = artist }
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
                            Text(artist, style = AppTypography.itemTitle, modifier = Modifier.weight(1f))

                            TrackActionMenu(
                                onPlay = { onPlayTracks(artistTracks) },
                                onPlayShuffle = { onPlayTracksShuffled(artistTracks) },
                                onPlayNext = { onPlayTracksNext(artistTracks) },
                                onAddToQueue = { onAddTracksToQueue(artistTracks) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumHeaderItem(
    albumName: String,
    artistName: String,
    artworkFileKey: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.bg3)
        ) {
            val imageUrl = LocalMcwsClient.current.buildImageUrl(artworkFileKey)
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = albumName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = albumName.ifEmpty { "Unknown Album" },
                style = AppTypography.itemTitle.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artistName.ifEmpty { "Unknown Artist" },
                style = AppTypography.itemSubtitle,
                color = AppColors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GroupedTrackRowItem(
    track: Track,
    indexInAlbum: Int,
    onClick: () -> Unit,
    onPlayTracks: (List<Track>) -> Unit,
    onPlayTracksShuffled: (List<Track>) -> Unit,
    onPlayTracksNext: (List<Track>) -> Unit,
    onAddTracksToQueue: (List<Track>) -> Unit,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bg2)
            .border(1.dp, AppColors.line, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val trackNum = if (track.trackNumber == 0) indexInAlbum + 1 else track.trackNumber
        Text(
            text = String.format(java.util.Locale.US, "%02d", trackNum),
            style = AppTypography.monoLabel.copy(color = AppColors.accent),
            modifier = Modifier.width(36.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                style = AppTypography.itemTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val durationSec = track.durationMs / 1000
            val timeStr = String.format(java.util.Locale.US, "%d:%02d", durationSec / 60, durationSec % 60)
            val subtitleText = if (track.artist != track.albumArtist) {
                "${track.artist} • $timeStr"
            } else {
                timeStr
            }
            Text(
                text = subtitleText,
                style = AppTypography.itemSubtitle,
                color = AppColors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (track.numberPlays > 0) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = "${track.numberPlays} plays",
                tint = AppColors.text3,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        TrackActionMenu(
            onPlay = { onPlayTracks(listOf(track)) },
            onPlayShuffle = { onPlayTracksShuffled(listOf(track)) },
            onPlayNext = { onPlayTracksNext(listOf(track)) },
            onAddToQueue = { onAddTracksToQueue(listOf(track)) },
            onInfoClick = onInfoClick
        )
    }
}

@Composable
fun TrackActionMenu(
    onPlay: () -> Unit,
    onPlayShuffle: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onInfoClick: (() -> Unit)? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.MoreVert,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = icon,
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
                text = { Text("Play Shuffle", style = AppTypography.itemTitle) },
                onClick = {
                    showMenu = false
                    onPlayShuffle()
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
                text = { Text("Add to Playing Queue", style = AppTypography.itemTitle) },
                onClick = {
                    showMenu = false
                    onAddToQueue()
                }
            )
            onInfoClick?.let {
                DropdownMenuItem(
                    text = { Text("Info", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        it()
                    }
                )
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
    isOffline: Boolean,
    onAlbumInfoClick: (Album) -> Unit
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
                    onInfoClick = { onAlbumInfoClick(album) },
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
    onInfoClick: (() -> Unit)? = null,
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
            val durationSec = track.durationMs / 1000
            val timeStr = String.format(java.util.Locale.US, "%d:%02d", durationSec / 60, durationSec % 60)
            Text(
                text = "${track.artist} • $timeStr",
                style = AppTypography.itemSubtitle,
                color = AppColors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (track.numberPlays > 0) {
            Icon(
                imageVector = Icons.Default.Headphones,
                contentDescription = "${track.numberPlays} plays",
                tint = AppColors.text3,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
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
    onInfoClick: (() -> Unit)? = null,
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
            Text(
                text = album.name,
                style = AppTypography.itemTitle.copy(fontSize = 14.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            val pathParts = listOf(album.parentFolderPath, album.folderPath)
                .flatMap { it.replace("\\", "/").split("/") }
                .filter { it.isNotEmpty() }
            val path = pathParts.takeLast(2).joinToString("/")
            if (path.isNotEmpty()) {
                Text(
                    text = path,
                    style = AppTypography.itemSubtitle.copy(fontSize = 11.sp),
                    color = AppColors.text3
                )
            }
            Text(
                text = album.date.ifEmpty { "Unknown Year" },
                style = AppTypography.itemSubtitle,
                color = AppColors.text2
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        var showMenu by remember { mutableStateOf(false) }
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
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
                onInfoClick?.let {
                    DropdownMenuItem(
                        text = { Text("Info", style = AppTypography.itemTitle) },
                        onClick = {
                            showMenu = false
                            it()
                        }
                    )
                }
            }
        }
    }
}
