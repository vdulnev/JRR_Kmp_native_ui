package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryViewState(
    val searchQuery: String = "",
    val searchResults: List<Track> = emptyList(),
    val currentTab: String = "artists",
    val artists: List<String> = emptyList(),
    val selectedArtist: String? = null,
    val artistAlbums: List<Album> = emptyList(),
    val randomAlbums: List<Album> = emptyList(),
    val browseStack: List<Pair<String, String>> = listOf(Pair("Library", "-1")),
    val browseChildren: Map<String, String> = emptyMap(),
    val browseTracks: List<Track> = emptyList(),
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
        // Observe offline status and sync tabs
        facade.activeZone
            .map { it.isOffline }
            .distinctUntilChanged()
            .onEach { isOffline ->
                _state.update { 
                    val nextTab = if (isOffline && (it.currentTab == "random" || it.currentTab == "browse")) {
                        "artists"
                    } else {
                        it.currentTab
                    }
                    it.copy(isOffline = isOffline, currentTab = nextTab) 
                }
                loadTabContent()
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val results = libraryRepository.searchFiles(query)
                    _state.update { it.copy(searchResults = results) }
                } catch (e: Exception) {
                    _state.update { it.copy(transientError = "Search failed: ${e.message ?: "unknown error"}") }
                }
            }
        } else {
            _state.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun switchTab(tab: String) {
        _state.update { it.copy(currentTab = tab, selectedArtist = null, artistAlbums = emptyList()) }
        loadTabContent()
    }

    fun selectArtist(artistName: String?) {
        _state.update { it.copy(selectedArtist = artistName) }
        if (artistName != null) {
            viewModelScope.launch {
                _state.update { it.copy(isTabLoading = true) }
                try {
                    val albums = libraryRepository.getAlbumsByArtist(artistName)
                    _state.update { it.copy(artistAlbums = albums, isTabLoading = false) }
                } catch (e: Exception) {
                    _state.update { it.copy(isTabLoading = false, transientError = "Failed to load albums: ${e.message}") }
                }
            }
        } else {
            _state.update { it.copy(artistAlbums = emptyList()) }
        }
    }

    fun pushBrowseNode(label: String, nodeId: String) {
        val currentStack = _state.value.browseStack.toMutableList()
        currentStack.add(Pair(label, nodeId))
        _state.update { it.copy(browseStack = currentStack) }
        loadBrowseNodeContent(nodeId)
    }

    fun popBrowseNode() {
        val currentStack = _state.value.browseStack.toMutableList()
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.size - 1)
            _state.update { it.copy(browseStack = currentStack) }
            loadBrowseNodeContent(currentStack.last().second)
        }
    }

    private fun loadBrowseNodeContent(nodeId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isTabLoading = true) }
            try {
                val children = libraryRepository.getBrowseChildren(nodeId)
                val tracks = libraryRepository.getBrowseFiles(nodeId)
                _state.update { it.copy(browseChildren = children, browseTracks = tracks, isTabLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isTabLoading = false, transientError = "Failed to load directory: ${e.message}") }
            }
        }
    }

    fun playTrack(track: Track) {
        try {
            facade.setQueue(listOf(track), 0)
            facade.play()
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Playback failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        try {
            facade.setQueue(tracks, startIndex)
            facade.play()
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Playback failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }

    fun retry() {
        loadTabContent()
    }

    private fun loadTabContent() {
        val currentState = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                when (currentState.currentTab) {
                    "artists" -> {
                        val artistsList = libraryRepository.getArtists()
                        _state.update { it.copy(artists = artistsList, isLoading = false) }
                    }
                    "random" -> {
                        val albums = libraryRepository.getRandomAlbums(20)
                        _state.update { it.copy(randomAlbums = albums, isLoading = false) }
                    }
                    "browse" -> {
                        val currentNode = currentState.browseStack.last()
                        val children = libraryRepository.getBrowseChildren(currentNode.second)
                        val tracks = libraryRepository.getBrowseFiles(currentNode.second)
                        _state.update { it.copy(browseChildren = children, browseTracks = tracks, isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, transientError = "Failed to load content: ${e.message}") }
            }
        }
    }
}
