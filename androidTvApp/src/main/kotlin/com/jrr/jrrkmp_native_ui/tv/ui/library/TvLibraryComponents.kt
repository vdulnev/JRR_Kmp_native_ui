@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.ArtistTrackGroup
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvAlbumCard
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvListRow
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvSearchBar
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrButtonColors
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrGold
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrMuted
import androidx.compose.foundation.lazy.grid.itemsIndexed as gridItemsIndexed

/** Screen heading + optional subtitle, consistent left margin across Library. */
@Composable
private fun Header(title: String, subtitle: String?) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(start = 48.dp, top = 8.dp, bottom = if (subtitle == null) 12.dp else 2.dp),
    )
    if (subtitle != null) {
        Text(
            text = subtitle,
            color = JrrMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 48.dp, bottom = 12.dp),
        )
    }
}

/**
 * A vertical list of single-line rows (e.g. artist names).
 *
 * The [LazyColumn] is rendered unconditionally — the loading/empty state is an
 * item *inside* it rather than a separate box that gets swapped out. That keeps
 * a stable focusable container across the load, so focus never escapes up to the
 * tab strip (which, with focus-to-switch tabs, would hijack navigation).
 */
@Composable
fun <T> TvRowListScreen(
    title: String,
    loader: suspend () -> List<T>,
    headlineOf: (T) -> String,
    onSelect: (T) -> Unit,
    loadKey: Any? = title,
    searchHint: String? = null,
    onSearchToggle: () -> Unit = {},
) {
    val items by produceState<List<T>?>(initialValue = null, loadKey) {
        value = runCatching { loader() }.getOrElse { emptyList() }
    }
    var query by remember(loadKey) { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        Header(title, null)
        if (searchHint != null) {
            TvSearchBar(
                query = query,
                onQueryChange = { query = it },
                hint = searchHint,
                modifier = Modifier.padding(horizontal = 48.dp).widthIn(max = 640.dp),
                onToggle = onSearchToggle,
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val list = items
            when {
                list == null -> item { TvListRow("Loading…", onClick = {}) }
                else -> {
                    val shown = list.filterByQuery(query, { headlineOf(it) })
                    when {
                        shown.isEmpty() -> item { TvListRow("Nothing here", onClick = {}) }
                        else -> itemsIndexed(shown) { _, item ->
                            TvListRow(headline = headlineOf(item), onClick = { onSelect(item) })
                        }
                    }
                }
            }
        }
    }
}

/** Case-insensitive substring filter over one or more text fields of [T]. */
private fun <T> List<T>.filterByQuery(query: String, vararg fields: (T) -> String): List<T> {
    val q = query.trim()
    if (q.isEmpty()) return this
    return filter { item -> fields.any { it(item).contains(q, ignoreCase = true) } }
}

/** A grid of album poster cards (stable container; loading is a full-width item). */
@Composable
fun TvAlbumGridScreen(
    title: String,
    subtitle: String?,
    loader: suspend () -> List<Album>,
    artworkOf: (Album) -> String?,
    onAlbum: (Album) -> Unit,
    loadKey: Any? = title,
    searchHint: String? = null,
    onSearchToggle: () -> Unit = {},
) {
    val albums by produceState<List<Album>?>(initialValue = null, loadKey) {
        value = runCatching { loader() }.getOrElse { emptyList() }
    }
    var query by remember(loadKey) { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        Header(title, subtitle)
        if (searchHint != null) {
            TvSearchBar(
                query = query,
                onQueryChange = { query = it },
                hint = searchHint,
                modifier = Modifier.padding(horizontal = 48.dp).widthIn(max = 640.dp),
                onToggle = onSearchToggle,
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(220.dp),
            contentPadding = PaddingValues(48.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            val list = albums?.filterByQuery(query, { it.name }, { it.albumArtist })
            when {
                list == null -> item(span = { GridItemSpan(maxLineSpan) }) {
                    TvListRow("Loading…", onClick = {})
                }
                list.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                    TvListRow("No albums", onClick = {})
                }
                else -> gridItemsIndexed(list) { _, album ->
                    TvAlbumCard(
                        title = album.name,
                        subtitle = album.albumArtist,
                        artworkUrl = artworkOf(album),
                        onClick = { onAlbum(album) },
                    )
                }
            }
        }
    }
}

/**
 * Track list with a leading "Play all" action (stable container; loading is an
 * item). Selecting a row plays the queue from there.
 */
@Composable
fun TvTrackListScreen(
    title: String,
    subtitle: String?,
    loader: suspend () -> List<Track>,
    onPlay: (tracks: List<Track>, startIndex: Int) -> Unit,
    loadKey: Any? = title,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null,
) {
    val tracks by produceState<List<Track>?>(initialValue = null, loadKey) {
        value = runCatching { loader() }.getOrElse { emptyList() }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Header(title, subtitle)
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val list = tracks
            when {
                list == null -> item { TvListRow("Loading…", onClick = {}) }
                list.isEmpty() -> item { TvListRow("No tracks", onClick = {}) }
                else -> {
                    item { TvListRow(headline = "▶  Play all", onClick = { onPlay(list, 0) }) }
                    if (onToggleFavorite != null) {
                        item {
                            TvListRow(
                                headline = if (isFavorite) "⭐  Remove Album from Favorites" else "⭐  Add Album to Favorites",
                                onClick = onToggleFavorite
                            )
                        }
                    }
                    itemsIndexed(list) { index, track ->
                        TvListRow(
                            headline = "${track.trackNumber}.  ${track.name}",
                            supporting = track.artist,
                            onClick = { onPlay(list, index) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Browse leaf track list with an optional **Group by Album Artist → Album** view
 * (with Expand all / Collapse all). Flat by default; grouping reuses the shared
 * multi-disc-aware grouping via [group].
 */
@Composable
fun TvBrowseLeaf(
    title: String,
    tracks: List<Track>,
    onPlay: (tracks: List<Track>, startIndex: Int) -> Unit,
    group: (List<Track>) -> List<ArtistTrackGroup>,
    notPlayed: (List<Track>) -> List<Track>,
    shuffle: (tracks: List<Track>, seed: Long) -> List<Track>,
) {
    var grouped by remember(title) { mutableStateOf(false) }
    var notPlayedOnly by remember(title) { mutableStateOf(false) }
    var shuffled by remember(title) { mutableStateOf(false) }
    var shuffleSeed by remember(title) { mutableStateOf(0L) }
    var expanded by remember(title) { mutableStateOf(emptySet<String>()) }
    // The repository owns the "not played" / "shuffle" rules; the UI just toggles
    // them on the already-loaded leaf/playlist tracks. Shuffling forces a flat
    // listing — artist/album grouping is meaningless once the order is random.
    val visibleTracks = remember(tracks, notPlayedOnly) {
        if (notPlayedOnly) notPlayed(tracks) else tracks
    }
    val orderedTracks = remember(visibleTracks, shuffled, shuffleSeed) {
        if (shuffled) shuffle(visibleTracks, shuffleSeed) else visibleTracks
    }
    val showGrouped = grouped && !shuffled
    val groups = remember(visibleTracks, showGrouped) { if (showGrouped) group(visibleTracks) else emptyList() }

    Column(modifier = Modifier.fillMaxSize()) {
        Header(title, null)
        Row(
            modifier = Modifier.padding(start = 48.dp, end = 48.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = {
                    if (!shuffled) shuffleSeed = System.currentTimeMillis()
                    shuffled = !shuffled
                },
                colors = jrrButtonColors(),
            ) { Text(if (shuffled) "Unshuffle" else "Shuffle") }
            Button(onClick = { notPlayedOnly = !notPlayedOnly }, colors = jrrButtonColors()) {
                Text(if (notPlayedOnly) "Show all" else "Show not played")
            }
            if (!shuffled) {
                Button(onClick = { grouped = !grouped }, colors = jrrButtonColors()) {
                    Text(if (grouped) "Ungroup" else "Group by Album Artist")
                }
            }
            if (showGrouped) {
                Button(
                    onClick = { expanded = groups.flatMap { g -> g.albums.map { it.groupId } }.toSet() },
                    colors = jrrButtonColors(),
                ) { Text("Expand all") }
                Button(onClick = { expanded = emptySet() }, colors = jrrButtonColors()) {
                    Text("Collapse all")
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!showGrouped) {
                if (orderedTracks.isEmpty()) {
                    item { TvListRow(if (notPlayedOnly) "No unplayed tracks" else "No tracks", onClick = {}) }
                } else {
                    item { TvListRow(headline = "▶  Play all", onClick = { onPlay(orderedTracks, 0) }) }
                    itemsIndexed(orderedTracks) { index, track ->
                        TvListRow(
                            headline = "${track.trackNumber}.  ${track.name}",
                            supporting = track.artist,
                            onClick = { onPlay(orderedTracks, index) },
                        )
                    }
                }
            } else {
                groups.forEach { artistGroup ->
                    item(key = "artist:${artistGroup.artist}") {
                        Text(
                            text = artistGroup.artist,
                            color = JrrGold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    }
                    artistGroup.albums.forEach { album ->
                        val isExpanded = album.groupId in expanded
                        item(key = "album:${album.groupId}") {
                            TvListRow(
                                headline = (if (isExpanded) "▾  " else "▸  ") + album.name.ifEmpty { "Unknown Album" },
                                supporting = "${album.tracks.size} tracks",
                                onClick = {
                                    expanded = if (isExpanded) expanded - album.groupId else expanded + album.groupId
                                },
                            )
                        }
                        if (isExpanded) {
                            itemsIndexed(
                                album.tracks,
                                key = { _, t -> "trk:${album.groupId}:${t.fileKey}" },
                            ) { index, track ->
                                TvListRow(
                                    headline = "      ${track.trackNumber}.  ${track.name}",
                                    supporting = track.artist,
                                    onClick = { onPlay(album.tracks, index) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
