@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import com.jrr.jrrkmp_native_ui.data.repository.BrowseContent
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.TvLibraryViewModel
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvListRow

// The Library sections are flat top-level tabs in TvMainScaffold; each is one of
// the drill-down flows below. `drill` is called on a drill-down click (lets the
// host briefly ignore focus-switch during the transition); `onDrillChange`
// reports whether the flow is currently drilled into an album/track list.

// --- Artists → Albums → Tracks ------------------------------------------------

@Composable
internal fun ArtistsFlow(vm: TvLibraryViewModel, drill: () -> Unit, onDrillChange: (Boolean) -> Unit) {
    var artist by remember { mutableStateOf<String?>(null) }
    var album by remember { mutableStateOf<Album?>(null) }
    LaunchedEffect(artist, album) { onDrillChange(artist != null || album != null) }

    val scope = rememberCoroutineScope()
    var isFavorite by remember(album) { mutableStateOf(false) }
    LaunchedEffect(album) {
        album?.let { a ->
            isFavorite = vm.isAlbumFavorite(a.albumGroupId)
        }
    }

    when {
        album != null -> {
            val a = album!!
            BackHandler { album = null }
            TvTrackListScreen(
                title = a.name,
                subtitle = a.albumArtist,
                loader = { vm.albumTracks(a) },
                onPlay = vm::play,
                loadKey = a.albumGroupId,
                isFavorite = isFavorite,
                onToggleFavorite = {
                    scope.launch {
                        val nowFav = vm.toggleAlbumFavorite(a)
                        isFavorite = nowFav
                    }
                }
            )
        }
        artist != null -> {
            val ar = artist!!
            BackHandler { artist = null }
            TvAlbumGridScreen(ar, null, { vm.albums(ar) }, { vm.artworkUrl(it.artworkFileKey) }, { drill(); album = it }, ar, searchHint = "Search albums", onSearchToggle = drill)
        }
        else -> TvRowListScreen("Artists", { vm.artists() }, { it }, { drill(); artist = it }, searchHint = "Search artists", onSearchToggle = drill)
    }
}

// --- Random albums → Tracks ---------------------------------------------------

@Composable
internal fun RandomAlbumsFlow(vm: TvLibraryViewModel, drill: () -> Unit, onDrillChange: (Boolean) -> Unit) {
    var album by remember { mutableStateOf<Album?>(null) }
    LaunchedEffect(album) { onDrillChange(album != null) }

    val scope = rememberCoroutineScope()
    var isFavorite by remember(album) { mutableStateOf(false) }
    LaunchedEffect(album) {
        album?.let { a ->
            isFavorite = vm.isAlbumFavorite(a.albumGroupId)
        }
    }

    if (album != null) {
        val a = album!!
        BackHandler { album = null }
        TvTrackListScreen(
            title = a.name,
            subtitle = a.albumArtist,
            loader = { vm.albumTracks(a) },
            onPlay = vm::play,
            loadKey = a.albumGroupId,
            isFavorite = isFavorite,
            onToggleFavorite = {
                scope.launch {
                    val nowFav = vm.toggleAlbumFavorite(a)
                    isFavorite = nowFav
                }
            }
        )
    } else {
        TvAlbumGridScreen("Random Albums", null, { vm.randomAlbums() }, { vm.artworkUrl(it.artworkFileKey) }, { drill(); album = it }, "random", searchHint = "Search albums", onSearchToggle = drill)
    }
}

// --- Browse: JRiver browse tree (children → … → leaf files) -------------------

private data class BrowseLevel(val id: String, val title: String)

@Composable
internal fun BrowseFlow(vm: TvLibraryViewModel, drill: () -> Unit, onDrillChange: (Boolean) -> Unit) {
    // Node stack through the JRiver browse hierarchy; root is "-1".
    val stack = remember { mutableStateListOf(BrowseLevel("-1", "Browse")) }
    val current = stack.last()
    LaunchedEffect(stack.size) { onDrillChange(stack.size > 1) }
    BackHandler(enabled = stack.size > 1) { stack.removeAt(stack.lastIndex) }

    // The repository (via the ViewModel) resolves whether this node has child
    // nodes or is a leaf (and fetches files only then) — the UI just renders it.
    val content by produceState<BrowseContent?>(initialValue = null, current.id) {
        value = runCatching { vm.browseNode(current.id) }.getOrNull()
    }

    when (val c = content) {
        null -> BrowseNodeList(current.title) { item { TvListRow("Loading…", onClick = {}) } }
        is BrowseContent.Nodes -> BrowseNodeList(current.title) {
            items(c.items, key = { it.key }) { node ->
                TvListRow(
                    headline = node.name,
                    onClick = { drill(); stack.add(BrowseLevel(node.key, node.name)) },
                )
            }
        }
        is BrowseContent.Files ->
            TvBrowseLeaf(current.title, c.tracks, vm::play, vm::group)
    }
}

@Composable
private fun BrowseNodeList(title: String, content: LazyListScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = title, modifier = Modifier.padding(start = 48.dp, top = 8.dp, bottom = 12.dp))
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content = content,
        )
    }
}

// --- Favorites → Tracks -------------------------------------------------------

@Composable
internal fun FavoritesFlow(vm: TvLibraryViewModel, drill: () -> Unit, onDrillChange: (Boolean) -> Unit) {
    var album by remember { mutableStateOf<Album?>(null) }
    LaunchedEffect(album) { onDrillChange(album != null) }

    val scope = rememberCoroutineScope()
    var isFavorite by remember(album) { mutableStateOf(false) }
    LaunchedEffect(album) {
        album?.let { a ->
            isFavorite = vm.isAlbumFavorite(a.albumGroupId)
        }
    }

    if (album != null) {
        val a = album!!
        BackHandler { album = null }
        TvTrackListScreen(
            title = a.name,
            subtitle = a.albumArtist,
            loader = { vm.albumTracks(a) },
            onPlay = vm::play,
            loadKey = a.albumGroupId,
            isFavorite = isFavorite,
            onToggleFavorite = {
                scope.launch {
                    val nowFav = vm.toggleAlbumFavorite(a)
                    isFavorite = nowFav
                }
            }
        )
    } else {
        val albums by produceState<List<Album>?>(initialValue = null, album) {
            value = runCatching { vm.favoriteAlbums() }.getOrElse { emptyList() }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            Text(text = "Favorites", modifier = Modifier.padding(start = 48.dp, top = 8.dp, bottom = 12.dp))
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val a = albums
                when {
                    a == null -> item { TvListRow("Loading…", onClick = {}) }
                    a.isEmpty() -> item { TvListRow("No favorites yet", onClick = {}) }
                    else -> items(a, key = { it.albumGroupId }) { alb ->
                        TvListRow(headline = alb.name, supporting = alb.albumArtist, onClick = { drill(); album = alb })
                    }
                }
            }
        }
    }
}
