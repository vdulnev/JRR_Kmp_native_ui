package com.jrr.jrrkmp_native_ui.presentation.screens

import com.jrr.jrrkmp_native_ui.presentation.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import com.jrr.jrrkmp_native_ui.presentation.components.InfoDialog
import com.jrr.jrrkmp_native_ui.presentation.components.toInfoFields
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.outlinedTextFieldColors
import com.jrr.jrrkmp_native_ui.data.api.BrowseItem
import com.jrr.jrrkmp_native_ui.data.api.BrowseNode
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.data.repository.groupTracksByArtistAndAlbum
import com.jrr.jrrkmp_native_ui.data.repository.albumGroupKeyOf
import com.jrr.jrrkmp_native_ui.presentation.components.AlphabetIndexBar
import com.jrr.jrrkmp_native_ui.presentation.components.sectionLetterFor
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onAlbumClick: (Album) -> Unit, // Album Name, Artist Name
    modifier: Modifier = Modifier,
    chromeCollapsed: Boolean = false,
    onChromeCollapsedChange: (Boolean) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val platformUi = com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi.current
    var infoTrack by remember { mutableStateOf<Track?>(null) }
    var infoAlbum by remember { mutableStateOf<Album?>(null) }

    // Detect scroll direction from any descendant list to collapse/expand the
    // chrome (header + filter here, mini-player in the host).
    val onCollapseChange by rememberUpdatedState(onChromeCollapsedChange)
    val scrollChromeConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val dy = available.y
                if (dy < -4f) onCollapseChange(true) // dragging content up → scrolling down
                else if (dy > 4f) onCollapseChange(false) // scrolling up
                return Offset.Zero
            }
        }
    }

    val artistsListState = rememberLazyListState()
    val artistAlbumsListState = rememberLazyListState()
    val compilationArtistsListState = rememberLazyListState()
    val randomAlbumsGridState = rememberLazyGridState()

    // Browse grouping toggle + per-album collapse state are hoisted here so
    // they survive switching away from and back to the Browse tab.
    var browseGrouped by remember { mutableStateOf(false) }
    val browseCollapsedAlbums = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            platformUi.showToast(error)
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
        // Top Toolbar — collapses while scrolling to maximise the list area.
        AnimatedVisibility(visible = !chromeCollapsed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Library".uppercase(),
                    style = AppTypography.screenTitle,
                    color = AppColors.text
                )
            }
        }

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
                .nestedScroll(scrollChromeConnection)
        ) {
            when (state.currentTab) {
                "artists" -> ArtistsTab(
                    artists = state.artists,
                    selectedArtist = state.selectedArtist,
                    artistAlbums = state.artistAlbums,
                    compilationMode = state.compilationMode,
                    compilationArtists = state.compilationArtists,
                    artistsFilter = state.artistsFilter,
                    chromeCollapsed = chromeCollapsed,
                    onFilterChange = { viewModel.setArtistsFilter(it) },
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
                    onBackClick = { viewModel.selectArtist(null) },
                    favorites = state.favorites,
                    onToggleFavoriteAlbum = { viewModel.toggleFavoriteAlbum(it) }
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
                    },
                    favorites = state.favorites,
                    onToggleFavoriteAlbum = { viewModel.toggleFavoriteAlbum(it) }
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
                    grouped = browseGrouped,
                    onGroupedChange = { browseGrouped = it },
                    collapsedAlbums = browseCollapsedAlbums,
                    onPlayTracks = { viewModel.playTracks(it, 0) },
                    onPlayTracksNext = { viewModel.playTracksNext(it) },
                    onAddTracksToQueue = { viewModel.addTracksToQueue(it) },
                    onDownloadTracks = { it.forEach(viewModel::downloadTrack) },
                    onRefresh = { viewModel.refreshBrowse() },
                    onBackClick = {
                        viewModel.popBrowseNode()
                    },
                    favorites = state.favorites,
                    onToggleFavorite = { viewModel.toggleFavoritePlaylist(it.key, it.name) },
                    onToggleFavoriteTrack = { viewModel.toggleFavoriteTrack(it) }
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
                    onAlbumInfoClick = { infoAlbum = it },
                    favorites = state.favorites,
                    onToggleFavoriteTrack = { viewModel.toggleFavoriteTrack(it) }
                )
                "favorites" -> FavoritesTab(
                    favorites = state.favorites,
                    onAlbumClick = onAlbumClick,
                    onPlayAlbum = { viewModel.playAlbum(it) },
                    onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                    onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                    onDownloadAlbum = { viewModel.downloadAlbum(it) },
                    isOffline = state.isOffline,
                    onAlbumInfoClick = { infoAlbum = it },
                    onPlaylistClick = { key, name ->
                        viewModel.switchTab("browse")
                        viewModel.pushBrowseNode(name, key)
                    },
                    onPlayPlaylist = { viewModel.playBrowseItem(it) },
                    onPlayPlaylistNext = { viewModel.playBrowseItemNext(it) },
                    onAddPlaylistToQueue = { viewModel.addBrowseItemToQueue(it) },
                    onDownloadPlaylist = { viewModel.downloadBrowseItem(it) },
                    onToggleFavorite = { viewModel.toggleFavoritePlaylist(it.key, it.name) },
                    onTrackClick = { viewModel.playTrack(it) },
                    onPlayTrack = { viewModel.playTrack(it) },
                    onPlayTrackNext = { viewModel.playTrackNext(it) },
                    onAddTrackToQueue = { viewModel.addTrackToQueue(it) },
                    onDownloadTrack = { viewModel.downloadTrack(it) },
                    onToggleFavoriteTrack = { viewModel.toggleFavoriteTrack(it) },
                    onTrackInfoClick = { infoTrack = it },
                    onToggleFavoriteAlbum = { viewModel.toggleFavoriteAlbum(it) }
                )
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
    artistsFilter: String,
    chromeCollapsed: Boolean,
    onFilterChange: (String) -> Unit,
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
    onBackClick: () -> Unit,
    favorites: List<com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity>,
    onToggleFavoriteAlbum: (Album) -> Unit
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

            ListFilterField(
                value = artistsFilter,
                onValueChange = onFilterChange,
                placeholder = "Filter albums",
                collapsed = chromeCollapsed
            )
            val filteredAlbums = remember(artistAlbums, artistsFilter) {
                if (artistsFilter.isBlank()) artistAlbums
                else artistAlbums.filter { it.name.contains(artistsFilter, ignoreCase = true) }
            }

            if (isLoadingAlbums) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else {
                val albumsScope = rememberCoroutineScope()
                val albumSections = remember(filteredAlbums) {
                    filteredAlbums.map { sectionLetterFor(it.name) }
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
                        items(filteredAlbums) { album ->
                            val isFavorite = favorites.any { it.type == "album" && it.identifier == "${album.name}|${album.albumArtist}" }
                            AlbumRowItem(
                                album = album,
                                isFavorite = isFavorite,
                                onToggleFavorite = { onToggleFavoriteAlbum(album) },
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
            ListFilterField(
                value = artistsFilter,
                onValueChange = onFilterChange,
                placeholder = "Filter artists",
                collapsed = chromeCollapsed
            )
            val filteredCompArtists = remember(compilationArtists, artistsFilter) {
                if (artistsFilter.isBlank()) compilationArtists
                else compilationArtists.filter { it.contains(artistsFilter, ignoreCase = true) }
            }
            if (isLoadingArtists) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else {
                val compScope = rememberCoroutineScope()
                val compSections = remember(filteredCompArtists) {
                    filteredCompArtists.map { sectionLetterFor(it) }
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
                        // Hide "All" while filtering — the user is hunting a
                        // specific artist, not the everything bucket.
                        if (artistsFilter.isBlank()) {
                            item {
                                CompilationArtistRow(
                                    label = AnnotatedString("All"),
                                    avatarText = "∗",
                                    highlighted = true,
                                    onClick = { onCompilationArtistClick(null) }
                                )
                            }
                        }
                        items(filteredCompArtists) { artist ->
                            CompilationArtistRow(
                                label = highlightMatch(artist, artistsFilter),
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
                            // +1 to skip the "All" row (only present when unfiltered).
                            val offset = if (artistsFilter.isBlank()) 1 else 0
                            if (idx >= 0) compScope.launch {
                                compilationArtistsListState.scrollToItem(idx + offset)
                            }
                        },
                    )
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            ListFilterField(
                value = artistsFilter,
                onValueChange = onFilterChange,
                placeholder = "Filter artists",
                collapsed = chromeCollapsed
            )
            val filteredArtists = remember(artists, artistsFilter) {
                if (artistsFilter.isBlank()) artists
                else artists.filter { it.contains(artistsFilter, ignoreCase = true) }
            }
            if (isLoadingArtists) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else {
                val scope = rememberCoroutineScope()
                val sections = remember(filteredArtists) { filteredArtists.map { sectionLetterFor(it) } }
                val letters = remember(sections) { sections.distinct() }
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = artistsListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 28.dp, top = 8.dp, bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredArtists) { artist ->
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
                                Text(highlightMatch(artist, artistsFilter), style = AppTypography.itemTitle)
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
}

/** Slim type-to-filter row pinned above a list. Collapses with the chrome. */
@Composable
internal fun ListFilterField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    collapsed: Boolean = false,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = !collapsed) {
        // Slim single-line filter field (≈40dp tall) instead of the chunky
        // OutlinedTextField, matching the design's compact filter row.
        Row(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.bg2)
                .border(1.dp, AppColors.line, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.text3, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, style = AppTypography.itemTitle.copy(fontSize = 14.sp), color = AppColors.text3)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = AppTypography.itemTitle.copy(fontSize = 14.sp, color = AppColors.text),
                    cursorBrush = SolidColor(AppColors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (value.isNotEmpty()) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).clickable { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear filter", tint = AppColors.text3, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Renders [text] with the first case-insensitive occurrence of [query] bolded
 * in the accent colour. No-op styling when [query] is blank or unmatched.
 */
internal fun highlightMatch(text: String, query: String): AnnotatedString = buildAnnotatedString {
    val idx = if (query.isBlank()) -1 else text.indexOf(query, ignoreCase = true)
    if (idx < 0) {
        append(text)
    } else {
        append(text.substring(0, idx))
        withStyle(SpanStyle(color = AppColors.accent, fontWeight = FontWeight.Bold)) {
            append(text.substring(idx, idx + query.length))
        }
        append(text.substring(idx + query.length))
    }
}

@Composable
internal fun CompilationArtistRow(
    label: AnnotatedString,
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
    onRefresh: () -> Unit,
    isLarge: Boolean = false,
    favorites: List<com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity>,
    onToggleFavoriteAlbum: (Album) -> Unit
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
        } else BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val pad = if (isLarge) 32.dp else 16.dp
            // One column per ~220dp of usable width: 2 on phones, 3 on a
            // foldable's inner display, more on wide tablets.
            val cols = ((maxWidth.value - pad.value * 2) / 220f).toInt().coerceAtLeast(2)
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(cols),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(pad),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(albums) { album ->
                    val isFavorite = favorites.any { it.type == "album" && it.identifier == "${album.name}|${album.albumArtist}" }
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

                            if (isFavorite) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favorited",
                                    tint = AppColors.accent,
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
                                        text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites", style = AppTypography.itemTitle) },
                                        onClick = {
                                            showMenu = false
                                            onToggleFavoriteAlbum(album)
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
    grouped: Boolean,
    onGroupedChange: (Boolean) -> Unit,
    collapsedAlbums: MutableMap<String, Boolean>,
    onPlayTracks: (List<Track>) -> Unit,
    onPlayTracksNext: (List<Track>) -> Unit,
    onAddTracksToQueue: (List<Track>) -> Unit,
    onDownloadTracks: (List<Track>) -> Unit,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    favorites: List<com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity>,
    onToggleFavorite: (BrowseItem) -> Unit,
    onToggleFavoriteTrack: (Track) -> Unit,
    isLarge: Boolean = false
) {
    // `grouped`: when on, a flat track listing is reorganised into Album Artist
    // → Album sections (multi-disc albums merged via albumGroupId). Hoisted by
    // the caller so it survives switching away from the Browse tab.
    val showingTracks = children.isEmpty() && tracks.isNotEmpty()

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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (showingTracks) {
                var showHeaderMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showHeaderMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Track list options",
                            tint = AppColors.text2,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showHeaderMenu,
                        onDismissRequest = { showHeaderMenu = false },
                        modifier = Modifier.background(AppColors.bg2)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh", style = AppTypography.itemTitle) },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = AppColors.text2) },
                            onClick = { showHeaderMenu = false; onRefresh() }
                        )
                        DropdownMenuItem(
                            text = { Text("Group by album artist", style = AppTypography.itemTitle) },
                            trailingIcon = {
                                if (grouped) Icon(Icons.Default.Check, contentDescription = "On", tint = AppColors.accent)
                            },
                            // Leave the menu open so the collapse/expand items appear immediately.
                            onClick = { onGroupedChange(!grouped) }
                        )
                        if (grouped) {
                            DropdownMenuItem(
                                text = { Text("Collapse all", style = AppTypography.itemTitle) },
                                leadingIcon = { Icon(Icons.Default.UnfoldLess, contentDescription = null, tint = AppColors.text2) },
                                onClick = {
                                    showHeaderMenu = false
                                    tracks.map { albumGroupKeyOf(it) }.distinct()
                                        .forEach { collapsedAlbums[it] = true }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Expand all", style = AppTypography.itemTitle) },
                                leadingIcon = { Icon(Icons.Default.UnfoldMore, contentDescription = null, tint = AppColors.text2) },
                                onClick = { showHeaderMenu = false; collapsedAlbums.clear() }
                            )
                        }
                    }
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.accent)
            }
        } else {
            // Large screens lay the folder/track lists out in two columns to use
            // the wider area; phones keep the single column.
            val cells = if (isLarge) GridCells.Fixed(2) else GridCells.Fixed(1)
            val pad = if (isLarge) 32.dp else 16.dp
            if (children.isNotEmpty()) {
                LazyVerticalGrid(
                    columns = cells,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = pad, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(children) { item ->
                        val isFav = favorites.any { it.type == "playlist" && it.identifier == item.key }
                        BrowseChildRow(
                            browseItem = BrowseItem(item.key, item.name),
                            isOffline = isOffline,
                            isFavorite = isFav,
                            onNodeClick = { onNodeClick(item.name, item.key) },
                            onPlay = onPlayBrowseItem,
                            onPlayNext = onPlayBrowseItemNext,
                            onAddToQueue = onAddBrowseItemToQueue,
                            onDownload = onDownloadBrowseItem,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }
            } else if (grouped) {
                BrowseGroupedTracks(
                    tracks = tracks,
                    pad = pad,
                    isOffline = isOffline,
                    collapsedAlbums = collapsedAlbums,
                    onTrackClick = onTrackClick,
                    onPlayTrack = onPlayTrack,
                    onPlayTrackNext = onPlayTrackNext,
                    onAddTrackToQueue = onAddTrackToQueue,
                    onDownloadTrack = onDownloadTrack,
                    onTrackInfoClick = onTrackInfoClick,
                    onPlayTracks = onPlayTracks,
                    onPlayTracksNext = onPlayTracksNext,
                    onAddTracksToQueue = onAddTracksToQueue,
                    onDownloadTracks = onDownloadTracks,
                    favorites = favorites,
                    onToggleFavoriteTrack = onToggleFavoriteTrack
                )
            } else {
                LazyVerticalGrid(
                    columns = cells,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = pad, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tracks) { track ->
                        val isFav = favorites.any { it.type == "track" && it.identifier == track.fileKey }
                        TrackRowItem(
                            track = track,
                            isFavorite = isFav,
                            onToggleFavorite = { onToggleFavoriteTrack(track) },
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

/**
 * Renders a flat browse track listing reorganised into Album Artist → Album
 * sections (see [groupByArtistAndAlbum]). Albums are keyed by
 * [Track.albumGroupId], which already folds the separate disc folders of a
 * multi-disc album into one group; within an album tracks are ordered by disc
 * then track number and split under "DISC N" sub-headers when more than one
 * disc is present (mirroring AlbumDetailScreen).
 */
@Composable
private fun BrowseGroupedTracks(
    tracks: List<Track>,
    pad: Dp,
    isOffline: Boolean,
    collapsedAlbums: MutableMap<String, Boolean>,
    onTrackClick: (Track, List<Track>) -> Unit,
    onPlayTrack: (Track) -> Unit,
    onPlayTrackNext: (Track) -> Unit,
    onAddTrackToQueue: (Track) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onTrackInfoClick: (Track) -> Unit,
    onPlayTracks: (List<Track>) -> Unit,
    onPlayTracksNext: (List<Track>) -> Unit,
    onAddTracksToQueue: (List<Track>) -> Unit,
    onDownloadTracks: (List<Track>) -> Unit,
    favorites: List<com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity>,
    onToggleFavoriteTrack: (Track) -> Unit
) {
    val artistGroups = remember(tracks) { groupTracksByArtistAndAlbum(tracks) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = pad, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        artistGroups.forEach { artistGroup ->
            item(key = "artist:${artistGroup.artist}") {
                val artistTracks = artistGroup.albums.flatMap { it.tracks }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = artistGroup.artist.uppercase(),
                        style = AppTypography.sectionLabel,
                        color = AppColors.accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    BrowseGroupActionMenu(
                        isOffline = isOffline,
                        onPlay = { onPlayTracks(artistTracks) },
                        onPlayNext = { onPlayTracksNext(artistTracks) },
                        onAddToQueue = { onAddTracksToQueue(artistTracks) },
                        onDownload = { onDownloadTracks(artistTracks) }
                    )
                }
            }
            artistGroup.albums.forEach { album ->
                val isCollapsed = collapsedAlbums[album.groupId] == true
                item(key = "album:${album.groupId}") {
                    AlbumHeaderItem(
                        albumName = album.name,
                        artistName = artistGroup.artist,
                        artworkFileKey = album.artworkFileKey,
                        collapsed = isCollapsed,
                        showArtwork = false,
                        onClick = { collapsedAlbums[album.groupId] = !isCollapsed },
                        trailing = {
                            BrowseGroupActionMenu(
                                isOffline = isOffline,
                                onPlay = { onPlayTracks(album.tracks) },
                                onPlayNext = { onPlayTracksNext(album.tracks) },
                                onAddToQueue = { onAddTracksToQueue(album.tracks) },
                                onDownload = { onDownloadTracks(album.tracks) }
                            )
                        }
                    )
                }
                if (!isCollapsed) {
                    val discGroups = album.tracks.groupBy { it.discNumber }
                    discGroups.forEach { (discNumber, discTracks) ->
                        if (discGroups.size > 1) {
                            item(key = "disc:${album.groupId}:$discNumber") {
                                Text(
                                    text = "DISC $discNumber",
                                    style = AppTypography.sectionLabel,
                                    color = AppColors.text3,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
                                )
                            }
                        }
                        items(discTracks, key = { it.fileKey }) { track ->
                            val isFav = favorites.any { it.type == "track" && it.identifier == track.fileKey }
                            TrackRowItem(
                                track = track,
                                isFavorite = isFav,
                                onToggleFavorite = { onToggleFavoriteTrack(track) },
                                onPlay = { onPlayTrack(track) },
                                onPlayNext = { onPlayTrackNext(track) },
                                onAddToQueue = { onAddTrackToQueue(track) },
                                onDownload = { onDownloadTrack(track) },
                                isOffline = isOffline,
                                onInfoClick = { onTrackInfoClick(track) },
                                onClick = { onTrackClick(track, album.tracks) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Overflow menu (Play / Play Next / Add to Queue / Download) for a grouped
 *  browse header — acts on all tracks of the artist or album. */
@Composable
private fun BrowseGroupActionMenu(
    isOffline: Boolean,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onDownload: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
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
            DropdownMenuItem(text = { Text("Play", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onPlay() })
            DropdownMenuItem(text = { Text("Play Next", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onPlayNext() })
            DropdownMenuItem(text = { Text("Add to Queue", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onAddToQueue() })
            if (!isOffline) {
                DropdownMenuItem(text = { Text("Download", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onDownload() })
            }
        }
    }
}

@Composable
private fun BrowseChildRow(
    browseItem: BrowseItem,
    isOffline: Boolean,
    isFavorite: Boolean,
    onNodeClick: () -> Unit,
    onPlay: (BrowseItem) -> Unit,
    onPlayNext: (BrowseItem) -> Unit,
    onAddToQueue: (BrowseItem) -> Unit,
    onDownload: (BrowseItem) -> Unit,
    onToggleFavorite: (BrowseItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bg2)
            .clickable { onNodeClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(browseItem.name, style = AppTypography.itemTitle, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)

        if (isFavorite) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorited",
                tint = AppColors.accent,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        var showMenu by remember { mutableStateOf(false) }
        Box {
            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
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
                DropdownMenuItem(text = { Text("Play", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onPlay(browseItem) })
                DropdownMenuItem(text = { Text("Play Next", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onPlayNext(browseItem) })
                DropdownMenuItem(text = { Text("Add to Queue", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onAddToQueue(browseItem) })
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites", style = AppTypography.itemTitle) },
                    onClick = { showMenu = false; onToggleFavorite(browseItem) }
                )
                if (!isOffline) {
                    DropdownMenuItem(text = { Text("Download", style = AppTypography.itemTitle) }, onClick = { showMenu = false; onDownload(browseItem) })
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
    onAlbumInfoClick: (Album) -> Unit,
    favorites: List<com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity> = emptyList(),
    onToggleFavoriteTrack: (Track) -> Unit = {}
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
                                val isFav = favorites.any { it.type == "track" && it.identifier == track.fileKey }
                                GroupedTrackRowItem(
                                    track = track,
                                    indexInAlbum = idx,
                                    isFavorite = isFav,
                                    onToggleFavorite = { onToggleFavoriteTrack(track) },
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
    modifier: Modifier = Modifier,
    collapsed: Boolean? = null,
    showArtwork: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showArtwork) {
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
        }
        Column(modifier = Modifier.weight(1f)) {
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
        trailing?.invoke()
        if (collapsed != null) {
            Icon(
                imageVector = if (collapsed) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                contentDescription = if (collapsed) "Expand album" else "Collapse album",
                tint = AppColors.text3,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun GroupedTrackRowItem(
    track: Track,
    indexInAlbum: Int,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
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
            text = trackNum.toString().padStart(2, '0'),
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
            val timeStr = "${durationSec / 60}:${(durationSec % 60).toString().padStart(2, '0')}"
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

        if (isFavorite) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorited",
                tint = AppColors.accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

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
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
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
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
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
            if (onToggleFavorite != null) {
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        onToggleFavorite()
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

@Composable
fun FavoritesTab(
    favorites: List<com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity>,
    onAlbumClick: (Album) -> Unit,
    onPlayAlbum: (Album) -> Unit,
    onPlayAlbumNext: (Album) -> Unit,
    onAddAlbumToQueue: (Album) -> Unit,
    onDownloadAlbum: (Album) -> Unit,
    isOffline: Boolean,
    onAlbumInfoClick: (Album) -> Unit,
    onPlaylistClick: (String, String) -> Unit,
    onPlayPlaylist: (BrowseItem) -> Unit,
    onPlayPlaylistNext: (BrowseItem) -> Unit,
    onAddPlaylistToQueue: (BrowseItem) -> Unit,
    onDownloadPlaylist: (BrowseItem) -> Unit,
    onToggleFavorite: (BrowseItem) -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayTrack: (Track) -> Unit,
    onPlayTrackNext: (Track) -> Unit,
    onAddTrackToQueue: (Track) -> Unit,
    onDownloadTrack: (Track) -> Unit,
    onToggleFavoriteTrack: (Track) -> Unit,
    onTrackInfoClick: (Track) -> Unit,
    onToggleFavoriteAlbum: (Album) -> Unit
) {
    val favoritedAlbums = favorites.filter { it.type == "album" }
    val favoritedPlaylists = favorites.filter { it.type == "playlist" }
    val favoritedTracks = favorites.filter { it.type == "track" }

    if (favoritedAlbums.isEmpty() && favoritedPlaylists.isEmpty() && favoritedTracks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Star, contentDescription = "Favorites", tint = AppColors.accent, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your Favorites", style = AppTypography.itemTitle)
                Text("Pinned albums, playlists and tracks will appear here", style = AppTypography.itemSubtitle, color = AppColors.text3)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (favoritedAlbums.isNotEmpty()) {
                item {
                    Text("Albums", style = AppTypography.sectionLabel, modifier = Modifier.padding(vertical = 8.dp))
                }
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
                        isFavorite = true,
                        onToggleFavorite = { onToggleFavoriteAlbum(album) },
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

            if (favoritedPlaylists.isNotEmpty()) {
                item {
                    Text("Playlists", style = AppTypography.sectionLabel, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(favoritedPlaylists) { fav ->
                    val browseItem = BrowseItem(fav.identifier, fav.displayName)
                    BrowseChildRow(
                        browseItem = browseItem,
                        isOffline = isOffline,
                        isFavorite = true,
                        onNodeClick = { onPlaylistClick(fav.identifier, fav.displayName) },
                        onPlay = onPlayPlaylist,
                        onPlayNext = onPlayPlaylistNext,
                        onAddToQueue = onAddPlaylistToQueue,
                        onDownload = onDownloadPlaylist,
                        onToggleFavorite = onToggleFavorite
                    )
                }
            }

            if (favoritedTracks.isNotEmpty()) {
                item {
                    Text("Tracks", style = AppTypography.sectionLabel, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(favoritedTracks) { fav ->
                    val parts = fav.displayName.split("|")
                    val name = parts.getOrNull(0) ?: fav.displayName
                    val artist = parts.getOrNull(1) ?: "Unknown Artist"
                    val album = parts.getOrNull(2) ?: "Unknown Album"
                    val durationMs = parts.getOrNull(3)?.toLongOrNull() ?: 0L
                    val track = Track(
                        fileKey = fav.identifier,
                        name = name,
                        artist = artist,
                        album = album,
                        albumArtist = "",
                        date = "",
                        genre = "",
                        durationMs = durationMs,
                        trackNumber = 0,
                        discNumber = 0,
                        totalDiscs = 0,
                        totalTracks = 0,
                        bitrate = 0,
                        bitDepth = 0,
                        sampleRate = 0,
                        channels = 0,
                        fileType = "",
                        filePath = "",
                        folderPath = ""
                    )
                    TrackRowItem(
                        track = track,
                        isFavorite = true,
                        onToggleFavorite = { onToggleFavoriteTrack(track) },
                        onPlay = { onPlayTrack(track) },
                        onPlayNext = { onPlayTrackNext(track) },
                        onAddToQueue = { onAddTrackToQueue(track) },
                        onDownload = { onDownloadTrack(track) },
                        isOffline = isOffline,
                        onInfoClick = { onTrackInfoClick(track) },
                        onClick = { onTrackClick(track) }
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRowItem(
    track: Track,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
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
            val timeStr = "${durationSec / 60}:${(durationSec % 60).toString().padStart(2, '0')}"
            Text(
                text = "${track.artist} • $timeStr",
                style = AppTypography.itemSubtitle,
                color = AppColors.text2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isFavorite) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorited",
                tint = AppColors.accent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

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
                DropdownMenuItem(
                    text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites", style = AppTypography.itemTitle) },
                    onClick = {
                        showMenu = false
                        onToggleFavorite()
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

@Composable
fun AlbumRowItem(
    album: Album,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
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

        if (isFavorite) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorited",
                tint = AppColors.accent,
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
                onToggleFavorite?.let {
                    DropdownMenuItem(
                        text = { Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites", style = AppTypography.itemTitle) },
                        onClick = {
                            showMenu = false
                            it()
                        }
                    )
                }
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
