package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.presentation.components.AlphabetIndexBar
import com.jrr.jrrkmp_native_ui.presentation.components.InfoDialog
import com.jrr.jrrkmp_native_ui.presentation.components.sectionLetterFor
import com.jrr.jrrkmp_native_ui.presentation.components.toInfoFields
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch

private val MASTER_WIDTH = 340.dp

/**
 * Large-screen Library: header + tab strip, with the Artists tab laid out as a
 * master/detail split (artist list left, that artist's albums right). Other tabs
 * reuse the phone tab bodies verbatim in the wider content area.
 *
 * Selecting an artist only updates [LibraryViewModel] state, so both panes stay
 * visible (no navigation). Opening an album pushes the album-detail child as on
 * phone — the sidebar lives outside this composable, so it persists.
 */
@Composable
fun LibraryLargeScreen(
    viewModel: LibraryViewModel,
    onAlbumClick: (Album) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var infoAlbum by remember { mutableStateOf<Album?>(null) }
    var infoTrack by remember { mutableStateOf<Track?>(null) }

    // Browse grouping toggle + per-album collapse state, hoisted so they
    // survive switching away from and back to the Browse tab.
    var browseGrouped by remember { mutableStateOf(false) }
    var browseNotPlayedOnly by remember { mutableStateOf(false) }
    var browseShuffled by remember { mutableStateOf(false) }
    var browseShuffleSeed by remember { mutableStateOf(0L) }
    val browseCollapsedAlbums = remember { mutableStateMapOf<String, Boolean>() }

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg1)) {
        // Header
        Column(modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 28.dp, bottom = 14.dp)) {
            Text("LIBRARY", style = AppTypography.sectionLabel)
            Spacer(Modifier.size(6.dp))
            Text("Browse", style = AppTypography.screenTitle)
        }

        val tabs = if (state.isOffline) {
            listOf("Artists" to "artists", "Downloads" to "downloads", "Favorites" to "favorites")
        } else {
            listOf(
                "Artists" to "artists",
                "Random" to "random",
                "Browse" to "browse",
                "Downloads" to "downloads",
                "Favorites" to "favorites",
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .drawBehind {
                    drawLine(
                        color = AppColors.line,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                },
        ) {
            tabs.forEach { (label, tabId) ->
                val selected = state.currentTab == tabId
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.switchTab(tabId) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        label.uppercase(),
                        style = AppTypography.chipMono.copy(color = if (selected) AppColors.accent else AppColors.text3),
                    )
                    Spacer(Modifier.size(8.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .size(2.dp)
                            .background(if (selected) AppColors.accent else Color.Transparent),
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state.currentTab) {
                "artists" -> ArtistsSplit(
                    viewModel = viewModel,
                    onAlbumClick = onAlbumClick,
                    onAlbumInfoClick = { infoAlbum = it },
                )
                "random" -> RandomTab(
                    albums = state.randomAlbums,
                    isLoading = state.isLoading,
                    gridState = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
                    onAlbumClick = onAlbumClick,
                    onPlayAlbum = { viewModel.playAlbum(it) },
                    onPlayAlbumNext = { viewModel.playAlbumNext(it) },
                    onAddAlbumToQueue = { viewModel.addAlbumToQueue(it) },
                    onDownloadAlbum = { viewModel.downloadAlbum(it) },
                    isOffline = state.isOffline,
                    onAlbumInfoClick = { infoAlbum = it },
                    onRefresh = { viewModel.retry() },
                    isLarge = true,
                    favorites = state.favorites,
                    onToggleFavoriteAlbum = { viewModel.toggleFavoriteAlbum(it) }
                )
                "browse" -> BrowseTab(
                    stack = state.browseStack,
                    children = state.browseChildren,
                    tracks = (if (browseNotPlayedOnly) viewModel.notPlayed(state.browseTracks) else state.browseTracks)
                        .let { if (browseShuffled) viewModel.shuffle(it, browseShuffleSeed) else it },
                    isLoading = state.isLoading || state.isTabLoading,
                    onNodeClick = { label, id -> viewModel.pushBrowseNode(label, id) },
                    onTrackClick = { clicked, all -> viewModel.playTracks(all, all.indexOf(clicked).coerceAtLeast(0)) },
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
                    notPlayedOnly = browseNotPlayedOnly,
                    onNotPlayedChange = { browseNotPlayedOnly = it },
                    shuffled = browseShuffled,
                    onShuffledChange = {
                        if (it) browseShuffleSeed = kotlin.random.Random.nextLong()
                        browseShuffled = it
                    },
                    collapsedAlbums = browseCollapsedAlbums,
                    onPlayTracks = { viewModel.playTracks(it, 0) },
                    onPlayTracksNext = { viewModel.playTracksNext(it) },
                    onAddTracksToQueue = { viewModel.addTracksToQueue(it) },
                    onDownloadTracks = { it.forEach(viewModel::downloadTrack) },
                    onRefresh = { viewModel.refreshBrowse() },
                    onBackClick = { viewModel.popBrowseNode() },
                    favorites = state.favorites,
                    onToggleFavorite = { viewModel.toggleFavoritePlaylist(it.key, it.name) },
                    onToggleFavoriteTrack = { viewModel.toggleFavoriteTrack(it) },
                    isLarge = true,
                )
                "downloads" -> DownloadsTab(
                    tracks = state.downloadedTracks,
                    isLoading = state.isLoading,
                    onTrackClick = { clicked, all -> viewModel.playTracks(all, all.indexOf(clicked).coerceAtLeast(0)) },
                    onPlayTracks = { viewModel.playTracks(it, 0) },
                    onPlayTracksShuffled = { viewModel.playTracksShuffled(it) },
                    onPlayTracksNext = { viewModel.playTracksNext(it) },
                    onAddTracksToQueue = { viewModel.addTracksToQueue(it) },
                    onTrackInfoClick = {},
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

    infoAlbum?.let { album ->
        InfoDialog(title = album.name, fields = album.toInfoFields(), onDismiss = { infoAlbum = null })
    }

    infoTrack?.let { track ->
        InfoDialog(title = track.name, fields = track.toInfoFields(), onDismiss = { infoTrack = null })
    }
}

@Composable
private fun ArtistsSplit(
    viewModel: LibraryViewModel,
    onAlbumClick: (Album) -> Unit,
    onAlbumInfoClick: (Album) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val masterListState = rememberLazyListState()

    Row(modifier = Modifier.fillMaxSize()) {
        // ---- Master pane: artists (or compilation artists) ----
        Column(
            modifier = Modifier
                .width(MASTER_WIDTH)
                .fillMaxHeight()
                .background(AppColors.bg1),
        ) {
            if (state.compilationMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectArtist(null) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppColors.accent)
                    Spacer(Modifier.width(8.dp))
                    Text("Compilations", style = AppTypography.subScreenTitle)
                }
            }
            ListFilterField(
                value = state.artistsFilter,
                onValueChange = { viewModel.setArtistsFilter(it) },
                placeholder = if (state.compilationMode) "Filter artists" else "Filter artists",
                collapsed = false,
            )
            if (state.isLoading || state.isTabLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.accent)
                }
            } else if (state.compilationMode) {
                val filtered = remember(state.compilationArtists, state.artistsFilter) {
                    if (state.artistsFilter.isBlank()) state.compilationArtists
                    else state.compilationArtists.filter { it.contains(state.artistsFilter, ignoreCase = true) }
                }
                LazyColumn(
                    state = masterListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.artistsFilter.isBlank()) {
                        item {
                            CompilationArtistRow(
                                label = AnnotatedString("All"),
                                avatarText = "∗",
                                highlighted = true,
                                onClick = { viewModel.selectCompilationArtist(null) },
                            )
                        }
                    }
                    items(filtered) { artist ->
                        CompilationArtistRow(
                            label = highlightMatch(artist, state.artistsFilter),
                            avatarText = artist.take(1).uppercase(),
                            highlighted = false,
                            onClick = { viewModel.selectCompilationArtist(artist) },
                        )
                    }
                }
            } else {
                val filtered = remember(state.artists, state.artistsFilter) {
                    if (state.artistsFilter.isBlank()) state.artists
                    else state.artists.filter { it.contains(state.artistsFilter, ignoreCase = true) }
                }
                val sections = remember(filtered) { filtered.map { sectionLetterFor(it) } }
                val letters = remember(sections) { sections.distinct() }
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = masterListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(filtered) { artist ->
                            MasterArtistRow(
                                name = artist,
                                selected = state.selectedArtist == artist,
                                query = state.artistsFilter,
                                onClick = { viewModel.selectArtist(artist) },
                            )
                        }
                    }
                    AlphabetIndexBar(
                        letters = letters,
                        onLetterSelected = { letter ->
                            val idx = sections.indexOf(letter)
                            if (idx >= 0) scope.launch { masterListState.scrollToItem(idx) }
                        },
                    )
                }
            }
        }

        Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AppColors.line))

        // ---- Detail pane: selected artist's albums ----
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            val selected = state.selectedArtist
            if (selected == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Select an artist", style = AppTypography.subScreenTitle, color = AppColors.text)
                    Spacer(Modifier.size(8.dp))
                    Text("Pick a name on the left to browse their albums.", style = AppTypography.itemSubtitle, color = AppColors.text3)
                }
            } else {
                // Album filter local to the detail pane, independent of the
                // master artist filter. Reset when the selected artist changes.
                var albumFilter by remember(selected) { mutableStateOf("") }
                val albums = remember(state.artistAlbums, albumFilter) {
                    if (albumFilter.isBlank()) state.artistAlbums
                    else state.artistAlbums.filter { it.name.contains(albumFilter, ignoreCase = true) }
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 32.dp, end = 32.dp, top = 22.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text(
                                "${albums.size} ${if (albums.size == 1) "Album" else "Albums"}",
                                style = AppTypography.sectionLabel,
                            )
                            Spacer(Modifier.size(4.dp))
                            Text(selected, style = AppTypography.subScreenTitle)
                        }
                        if (albums.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.accent)
                                    .clickable {
                                        viewModel.playAlbum(albums.first())
                                        albums.drop(1).forEach { viewModel.addAlbumToQueue(it) }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AppColors.bg0, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("PLAY ALL", style = AppTypography.chipMono.copy(color = AppColors.bg0, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        ListFilterField(
                            value = albumFilter,
                            onValueChange = { albumFilter = it },
                            placeholder = "Filter albums",
                            collapsed = false,
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 12.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(albums) { album ->
                            val isFavorite = state.favorites.any { it.type == "album" && it.identifier == album.albumGroupId }
                            AlbumRowItem(
                                album = album,
                                isFavorite = isFavorite,
                                onToggleFavorite = { viewModel.toggleFavoriteAlbum(album) },
                                onPlay = { viewModel.playAlbum(album) },
                                onPlayNext = { viewModel.playAlbumNext(album) },
                                onAddToQueue = { viewModel.addAlbumToQueue(album) },
                                onDownload = { viewModel.downloadAlbum(album) },
                                isOffline = state.isOffline,
                                onInfoClick = { onAlbumInfoClick(album) },
                                onClick = { onAlbumClick(album) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasterArtistRow(
    name: String,
    selected: Boolean,
    query: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) AppColors.accentDim else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) AppColors.accent else AppColors.bg3),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.removePrefix("The ").take(1).uppercase(),
                style = AppTypography.chipMono.copy(color = if (selected) AppColors.bg0 else AppColors.accent, fontSize = 14.sp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            highlightMatch(name, query),
            style = AppTypography.itemTitle.copy(color = if (selected) AppColors.accent else AppColors.text),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
