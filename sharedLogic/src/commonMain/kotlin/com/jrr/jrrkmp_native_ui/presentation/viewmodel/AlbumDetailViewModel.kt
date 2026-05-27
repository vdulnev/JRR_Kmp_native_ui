package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Flat view state — was previously a `sealed interface AlbumDetailContentState`
 * of (Loading, Success, Error). The sealed shape forced `as?` casts on the
 * Swift side and a silent "else means Loading" branch that would silently
 * misbehave if a fourth case were added. The shape below is exhaustive by
 * construction:
 *
 * - Loading      => isLoading = true, errorMessage = null
 * - Loaded       => isLoading = false, errorMessage = null, tracks/... populated
 * - Error        => isLoading = false, errorMessage != null
 */
data class AlbumDetailViewState(
    val albumName: String,
    val artistName: String,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val tracks: List<Track> = emptyList(),
    val downloadedTrackKeys: Set<String> = emptySet(),
    val activeDownloadJobs: Map<String, String> = emptyMap(),
    val isFavorite: Boolean = false,
    val isOfflineMode: Boolean = true,
    val transientError: String? = null,
)

class AlbumDetailViewModel(
    val album: Album,
    private val libraryRepository: LibraryRepository,
    private val facade: AudioPlayerFacade,
    private val database: JrrDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumDetailViewState(album.name, album.albumArtist))
    val state: StateFlow<AlbumDetailViewState> = _state.asStateFlow()

    // Internal state updates
    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())
    private val favoriteFlow = MutableStateFlow(false)

    init {
        // Observe database updates for downloads, jobs, and favorites
        combine(
            tracksFlow,
            database.downloadedTrackDao().getAllTracksFlow(),
            database.downloadJobDao().getAllJobsFlow(),
            favoriteFlow,
            facade.activeZone
        ) { tracks, downloaded, jobs, favorite, activeZone ->
            val downloadedKeys = downloaded.map { it.fileKey }.toSet()
            val activeJobs = jobs.associate { it.fileKey to it.state }
            val isOffline = activeZone.isOffline || facade.currentServerHost.isNullOrEmpty()

            _state.update { currentState ->
                // Don't overwrite an error state with fresh data — the user
                // needs to retry first.
                if (currentState.errorMessage != null) {
                    currentState.copy(isOfflineMode = isOffline)
                } else {
                    currentState.copy(
                        isLoading = false,
                        tracks = tracks,
                        downloadedTrackKeys = downloadedKeys,
                        activeDownloadJobs = activeJobs,
                        isFavorite = favorite,
                        isOfflineMode = isOffline,
                    )
                }
            }
        }.launchIn(viewModelScope)

        refreshTracksAndFavorite()
    }

    private fun refreshTracksAndFavorite() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Fetch tracks from repository
                val albumTracks = libraryRepository.getAlbumTracks(album)
                tracksFlow.value = albumTracks

                // Check favorite status in DB
                val identifier = "${album.name}|${album.albumArtist}"
                val favorite = database.favoriteDao().getFavorite("album", identifier) != null
                favoriteFlow.value = favorite
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to load album tracks",
                    )
                }
            }
        }
    }

    fun playTrack(track: Track) {
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            val startIndex = currentTracks.indexOf(track).coerceAtLeast(0)
            facade.setQueue(currentTracks, startIndex)
            facade.play()
        }
    }

    fun playAlbum() {
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            facade.setQueue(currentTracks, 0)
            facade.play()
        }
    }

    fun shuffleAlbum() {
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            facade.setQueue(currentTracks.shuffled(), 0)
            facade.play()
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val identifier = "${album.name}|${album.albumArtist}"
                val dao = database.favoriteDao()
                val existing = dao.getFavorite("album", identifier)
                if (existing != null) {
                    dao.delete(existing)
                    favoriteFlow.value = false
                } else {
                    val newFav = FavoriteEntity(
                        type = "album",
                        identifier = identifier,
                        displayName = album.name,
                        addedAt = getTimeMillis()
                    )
                    dao.insert(newFav)
                    favoriteFlow.value = true
                }
            } catch (e: Exception) {
                _state.update { it.copy(transientError = "Failed to toggle favorite: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun startDownload(track: Track) {
        viewModelScope.launch {
            try {
                libraryRepository.startDownload(track)
            } catch (e: Exception) {
                _state.update { it.copy(transientError = "Failed to start download: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }

    fun retry() {
        refreshTracksAndFavorite()
    }

    /**
     * iOS-side teardown. Cancels `viewModelScope` so the collectors started in
     * `init` and any in-flight `viewModelScope.launch` jobs stop holding the VM
     * alive via their captured lambdas.
     *
     * On Android, `ViewModelStore.clear()` triggers the same cancellation via
     * `onCleared()`; calling `dispose()` first is a no-op there since the scope
     * is already inert by then.
     *
     * Must be called from the Swift `AlbumDetailObservable.deinit` — SwiftUI's
     * `@State`-held wrappers go away non-deterministically without this hook.
     */
    fun dispose() {
        viewModelScope.cancel()
    }
}
