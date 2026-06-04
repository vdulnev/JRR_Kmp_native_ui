package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.data.api.BrowseItem
import com.jrr.jrrkmp_native_ui.data.api.BrowseNode
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.MULTIPLE_ARTISTS_SENTINEL
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val log = Logger.withTag("vm:Library")

private fun LibraryViewState.summary(): String = buildString {
    append("tab=$currentTab")
    append(" artists=${artists.size}")
    if (selectedArtist != null) append(" sel=$selectedArtist albums=${artistAlbums.size}")
    if (randomAlbums.isNotEmpty()) append(" random=${randomAlbums.size}")
    if (browseStack.size > 1) append(" browse=${browseStack.joinToString("/") { it.label }}")
    if (browseChildren.isNotEmpty()) append(" children=${browseChildren.size}")
    if (browseTracks.isNotEmpty()) append(" browseTracks=${browseTracks.size}")
    append(" offline=$isOffline loading=$isLoading tabLoading=$isTabLoading")
    if (transientError != null) append(" err=$transientError")
}

data class LibraryViewState(
    val currentTab: String = "artists",
    val artists: List<String> = emptyList(),
    val selectedArtist: String? = null,
    val artistAlbums: List<Album> = emptyList(),
    // Compilations drill-down (the "(Multiple Artists)" branch). When
    // [compilationMode] is true and [selectedArtist] is null, the UI shows the
    // intermediate "contributing artists" list ([compilationArtists]) with an
    // "All" entry; picking one loads [artistAlbums] of the matching compilations.
    val compilationMode: Boolean = false,
    val compilationArtists: List<String> = emptyList(),
    // Quick type-to-filter applied to whichever Artists-tab list is showing
    // (artists / compilation artists / albums). Reset whenever the displayed
    // list changes.
    val artistsFilter: String = "",
    val randomAlbums: List<Album> = emptyList(),
    val browseStack: List<BrowseNode> = listOf(BrowseNode("Library", "-1")),
    val browseChildren: List<BrowseItem> = emptyList(),
    val browseTracks: List<Track> = emptyList(),
    val downloadedTracks: List<Track> = emptyList(),
    val isOffline: Boolean = false,
    val isLoading: Boolean = false,
    val isTabLoading: Boolean = false,
    val transientError: String? = null
)

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val facade: AudioPlayerFacade
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryViewState())
    val state: StateFlow<LibraryViewState> = _state.asStateFlow()

    init {
        log.d { "init" }
        // Observe offline status and server connection token to sync tabs and reload content
        combine(
            facade.activeZone.map { it.isOffline }.distinctUntilChanged(),
            facade.connectionToken
        ) { isOffline, token ->
            Pair(isOffline, token)
        }
            .distinctUntilChanged()
            .onEach { (isOffline, _) ->
                log.d { "connection change isOffline=$isOffline" }
                // Only wipe the drill-down when offline-ness actually flips —
                // not on a mere token refresh while staying online — so we don't
                // yank the user out of an artist they're browsing.
                val offlineFlipped = _state.value.isOffline != isOffline
                _state.update {
                    val nextTab =
                        if (isOffline && (it.currentTab == "random" || it.currentTab == "browse")) {
                            log.d { "force tab → artists (was=${it.currentTab}, offline)" }
                            "artists"
                        } else {
                            it.currentTab
                        }
                    it.copy(
                        isOffline = isOffline,
                        currentTab = nextTab,
                        // Drop any drill-down / search loaded for the previous
                        // connection state. Otherwise an artist's album grid
                        // browsed online (with un-downloaded server albums) stays
                        // visible after going offline and fails to play.
                        selectedArtist = if (offlineFlipped) null else it.selectedArtist,
                        artistAlbums = if (offlineFlipped) emptyList() else it.artistAlbums,
                        compilationMode = if (offlineFlipped) false else it.compilationMode,
                        compilationArtists = if (offlineFlipped) emptyList() else it.compilationArtists,
                        artistsFilter = if (offlineFlipped) "" else it.artistsFilter,
                    )
                }
                loadTabContent()
            }
            .launchIn(viewModelScope)

        // Mirror state to Verbose for change tracing.
        state.logged(log, "state") { it.summary() }.launchIn(viewModelScope)
    }

    fun switchTab(tab: String) {
        log.d { "switchTab($tab)" }
        _state.update {
            it.copy(
                currentTab = tab,
                selectedArtist = null,
                artistAlbums = emptyList(),
                compilationMode = false,
                compilationArtists = emptyList(),
                artistsFilter = "",
            )
        }
        loadTabContent()
    }

    /** Quick type-to-filter for the active Artists-tab list. */
    fun setArtistsFilter(query: String) {
        _state.update { it.copy(artistsFilter = query) }
    }

    /**
     * Top-level artist-row tap. `null` acts as a back gesture. Tapping the
     * compilations row ([MULTIPLE_ARTISTS_SENTINEL]) opens the compilations
     * drill-down instead of loading albums directly.
     */
    fun selectArtist(artistName: String?) {
        log.d { "selectArtist($artistName)" }
        when {
            artistName == null -> artistsBack()
            artistName.equals(MULTIPLE_ARTISTS_SENTINEL, ignoreCase = true) -> openCompilations()
            else -> {
                _state.update { it.copy(selectedArtist = artistName, artistsFilter = "") }
                loadArtistAlbums { libraryRepository.getAlbumsByArtist(artistName) }
            }
        }
    }

    /** Enter the compilations branch: show the contributing-artists list. */
    private fun openCompilations() {
        log.d { "openCompilations()" }
        _state.update {
            it.copy(
                compilationMode = true,
                selectedArtist = null,
                artistAlbums = emptyList(),
                artistsFilter = "",
                isTabLoading = true,
            )
        }
        viewModelScope.launch {
            try {
                val artists = libraryRepository.getCompilationArtists()
                log.d { "loaded ${artists.size} compilation artists" }
                _state.update { it.copy(compilationArtists = artists, isTabLoading = false) }
            } catch (e: Exception) {
                log.e(e) { "openCompilations failed" }
                _state.update {
                    it.copy(
                        isTabLoading = false,
                        transientError = "Failed to load compilation artists: ${e.message}",
                    )
                }
            }
        }
    }

    /**
     * Pick an entry from the compilations contributing-artists list. `null`
     * selects "All" (every compilation); otherwise the compilations that
     * contain [artistName].
     */
    fun selectCompilationArtist(artistName: String?) {
        log.d { "selectCompilationArtist($artistName)" }
        _state.update {
            it.copy(selectedArtist = artistName ?: MULTIPLE_ARTISTS_SENTINEL, artistsFilter = "")
        }
        loadArtistAlbums {
            if (artistName == null) {
                libraryRepository.getAlbumsByArtist(MULTIPLE_ARTISTS_SENTINEL)
            } else {
                libraryRepository.getCompilationAlbumsByArtist(artistName)
            }
        }
    }

    /** Back within the Artists tab — unwinds the compilations drill-down too. */
    private fun artistsBack() {
        _state.update {
            when {
                it.compilationMode && it.selectedArtist != null ->
                    it.copy(selectedArtist = null, artistAlbums = emptyList(), artistsFilter = "")
                it.compilationMode ->
                    it.copy(
                        compilationMode = false,
                        compilationArtists = emptyList(),
                        artistsFilter = "",
                    )
                else ->
                    it.copy(selectedArtist = null, artistAlbums = emptyList(), artistsFilter = "")
            }
        }
    }

    private fun loadArtistAlbums(fetch: suspend () -> List<Album>) {
        viewModelScope.launch {
            _state.update { it.copy(isTabLoading = true) }
            try {
                val albums = fetch()
                log.d { "loaded ${albums.size} albums" }
                _state.update { it.copy(artistAlbums = albums, isTabLoading = false) }
            } catch (e: Exception) {
                log.e(e) { "loadArtistAlbums failed" }
                _state.update {
                    it.copy(
                        isTabLoading = false,
                        transientError = "Failed to load albums: ${e.message}",
                    )
                }
            }
        }
    }

    fun pushBrowseNode(label: String, nodeId: String) {
        log.d { "pushBrowseNode(label=$label, nodeId=$nodeId)" }
        val currentStack = _state.value.browseStack.toMutableList()
        currentStack.add(BrowseNode(label, nodeId))
        _state.update { it.copy(browseStack = currentStack) }
        loadBrowseNodeContent(nodeId)
    }

    fun popBrowseNode() {
        log.d { "popBrowseNode() depth=${_state.value.browseStack.size}" }
        val currentStack = _state.value.browseStack.toMutableList()
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.size - 1)
            _state.update { it.copy(browseStack = currentStack) }
            loadBrowseNodeContent(currentStack.last().nodeId)
        }
    }

    /** Re-fetch the children/tracks of the current browse node. */
    fun refreshBrowse() {
        val current = _state.value.browseStack.lastOrNull() ?: return
        log.d { "refreshBrowse(nodeId=${current.nodeId})" }
        loadBrowseNodeContent(current.nodeId)
    }

    private fun loadBrowseNodeContent(nodeId: String) {
        viewModelScope.launch {
            log.d { "loadBrowseNodeContent(nodeId=$nodeId)" }
            _state.update { it.copy(isTabLoading = true) }
            try {
                val (children, tracks) = getChildrenAndTracks(nodeId)
                log.d { "browse → ${children.size} children, ${tracks.size} tracks" }
                _state.update {
                    it.copy(
                        browseChildren = children,
                        browseTracks = tracks,
                        isTabLoading = false
                    )
                }
            } catch (e: Exception) {
                log.e(e) { "loadBrowseNodeContent failed nodeId=$nodeId" }
                _state.update {
                    it.copy(
                        isTabLoading = false,
                        transientError = "Failed to load directory: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun getChildrenAndTracks(nodeId: String): Pair<List<BrowseItem>, List<Track>> {
        val children =
            libraryRepository.getBrowseChildren(nodeId)
        val tracks = if (children.isEmpty()) {
            libraryRepository.getBrowseFiles(nodeId)
        } else {
            emptyList()
        }
        return Pair(children, tracks)
    }

    fun playTrack(track: Track) {
        log.d { "playTrack(${track.fileKey} / ${track.name})" }
        try {
            facade.setQueue(listOf(track), 0)
            facade.play()
        } catch (e: Exception) {
            log.e(e) { "playTrack failed fileKey=${track.fileKey}" }
            _state.update { it.copy(transientError = "Playback failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        log.d { "playTracks(${tracks.size} tracks, startIndex=$startIndex)" }
        try {
            facade.setQueue(tracks, startIndex)
            facade.play()
        } catch (e: Exception) {
            log.e(e) { "playTracks failed" }
            _state.update { it.copy(transientError = "Playback failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun playTracksShuffled(tracks: List<Track>) {
        playTracks(tracks.shuffled(), 0)
    }

    fun playTracksNext(tracks: List<Track>) {
        facade.playNextTracks(tracks)
    }

    fun addTracksToQueue(tracks: List<Track>) {
        facade.addTracks(tracks)
    }

    fun addTrackToQueue(track: Track) {
        log.d { "addTrackToQueue(${track.fileKey})" }
        facade.addTracks(listOf(track))
    }

    fun playTrackNext(track: Track) {
        log.d { "playTrackNext(${track.fileKey})" }
        facade.playNextTracks(listOf(track))
    }

    fun downloadTrack(track: Track) {
        log.d { "downloadTrack(${track.fileKey} / ${track.name})" }
        viewModelScope.launch {
            try {
                libraryRepository.startDownload(track)
            } catch (e: Exception) {
                log.e(e) { "downloadTrack failed fileKey=${track.fileKey}" }
                _state.update { it.copy(transientError = "Download failed: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun playAlbum(album: Album) {
        log.d { "playAlbum(${album.name} / ${album.albumArtist})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getAlbumTracks(album)
                if (tracks.isNotEmpty()) {
                    facade.setQueue(tracks, 0)
                    facade.play()
                }
            } catch (e: Exception) {
                log.e(e) { "playAlbum failed album=${album.name}" }
                _state.update { it.copy(transientError = "Playback failed: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun addAlbumToQueue(album: Album) {
        log.d { "addAlbumToQueue(${album.name})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getAlbumTracks(album)
                if (tracks.isNotEmpty()) {
                    facade.addTracks(tracks)
                }
            } catch (e: Exception) {
                log.e(e) { "addAlbumToQueue failed album=${album.name}" }
                _state.update { it.copy(transientError = "Failed to add album: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun playAlbumNext(album: Album) {
        log.d { "playAlbumNext(${album.name})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getAlbumTracks(album)
                if (tracks.isNotEmpty()) {
                    facade.playNextTracks(tracks)
                }
            } catch (e: Exception) {
                log.e(e) { "playAlbumNext failed album=${album.name}" }
                _state.update { it.copy(transientError = "Failed to play album next: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun downloadAlbum(album: Album) {
        log.d { "downloadAlbum(${album.name})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getAlbumTracks(album)
                tracks.forEach { libraryRepository.startDownload(it) }
            } catch (e: Exception) {
                log.e(e) { "downloadAlbum failed album=${album.name}" }
                _state.update { it.copy(transientError = "Download failed: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun playBrowseItem(item: BrowseItem) {
        log.d { "playBrowseItem(${item.key} / ${item.name})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getBrowseFiles(item.key)
                if (tracks.isNotEmpty()) {
                    facade.setQueue(tracks, 0)
                    facade.play()
                }
            } catch (e: Exception) {
                log.e(e) { "playBrowseItem failed key=${item.key}" }
                _state.update { it.copy(transientError = "Playback failed: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun addBrowseItemToQueue(item: BrowseItem) {
        log.d { "addBrowseItemToQueue(${item.key})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getBrowseFiles(item.key)
                if (tracks.isNotEmpty()) {
                    facade.addTracks(tracks)
                }
            } catch (e: Exception) {
                log.e(e) { "addBrowseItemToQueue failed key=${item.key}" }
                _state.update { it.copy(transientError = "Failed to add playlist: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun playBrowseItemNext(item: BrowseItem) {
        log.d { "playBrowseItemNext(${item.key})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getBrowseFiles(item.key)
                if (tracks.isNotEmpty()) {
                    facade.playNextTracks(tracks)
                }
            } catch (e: Exception) {
                log.e(e) { "playBrowseItemNext failed key=${item.key}" }
                _state.update { it.copy(transientError = "Failed to play playlist next: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun downloadBrowseItem(item: BrowseItem) {
        log.d { "downloadBrowseItem(${item.key})" }
        viewModelScope.launch {
            try {
                val tracks = libraryRepository.getBrowseFiles(item.key)
                tracks.forEach { libraryRepository.startDownload(it) }
            } catch (e: Exception) {
                log.e(e) { "downloadBrowseItem failed key=${item.key}" }
                _state.update { it.copy(transientError = "Download failed: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }

    fun retry() {
        log.d { "retry()" }
        loadTabContent()
    }

    private fun loadTabContent() {
        val currentState = _state.value
        log.d { "loadTabContent tab=${currentState.currentTab}" }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                when (currentState.currentTab) {
                    "artists" -> {
                        val artistsList = libraryRepository.getArtists()
                        log.d { "loaded ${artistsList.size} artists" }
                        _state.update { it.copy(artists = artistsList, isLoading = false) }
                    }

                    "random" -> {
                        val albums = libraryRepository.getRandomAlbums(20)
                        log.d { "loaded ${albums.size} random albums" }
                        _state.update { it.copy(randomAlbums = albums, isLoading = false) }
                    }

                    "browse" -> {
                        val currentNode = currentState.browseStack.last()
                        val (children, tracks) = getChildrenAndTracks(currentNode.nodeId)
                        log.d { "browse → ${children.size} children, ${tracks.size} tracks" }
                        _state.update {
                            it.copy(
                                browseChildren = children,
                                browseTracks = tracks,
                                isLoading = false
                            )
                        }
                    }

                    "downloads" -> {
                        val downloaded = libraryRepository.getDownloadedTracks()
                        log.d { "loaded ${downloaded.size} downloaded tracks" }
                        _state.update {
                            it.copy(
                                downloadedTracks = downloaded,
                                isLoading = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log.e(e) { "loadTabContent failed tab=${currentState.currentTab}" }
                _state.update {
                    it.copy(
                        isLoading = false,
                        transientError = "Failed to load content: ${e.message}"
                    )
                }
            }
        }
    }
}
