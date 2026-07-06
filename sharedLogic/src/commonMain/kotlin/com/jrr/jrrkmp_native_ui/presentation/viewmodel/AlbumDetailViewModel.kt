package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.coroutines.parZip
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity
import com.jrr.jrrkmp_native_ui.data.repository.ArtistInfoRepository
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val log = Logger.withTag("vm:AlbumDetail")

private fun AlbumDetailViewState.summary(): String = buildString {
    append("content=")
    append(
        when (val c = contentState) {
            AlbumDetailContentState.Loading -> "Loading"
            is AlbumDetailContentState.Success -> "Success(tracks=${c.tracks.size} downloaded=${c.downloadedTrackKeys.size} jobs=${c.activeDownloadJobs.size} fav=${c.isFavorite})"
            is AlbumDetailContentState.Error -> "Error(${c.message})"
        },
    )
    append(" offline=$isOfflineMode")
    if (transientError != null) append(" err=$transientError")
}

sealed interface AlbumDetailContentState {
    data object Loading : AlbumDetailContentState
    data class Success(
        val tracks: List<Track> = emptyList(),
        val downloadedTrackKeys: Set<String> = emptySet(),
        val activeDownloadJobs: Map<String, String> = emptyMap(),
        val isFavorite: Boolean = false,
        val favoritedTrackKeys: Set<String> = emptySet(),
        // Header cover URL, built by the VM (the UI no longer reaches into
        // McwsClient). Derived from the first track's key.
        val headerArtworkUrl: String = "",
        // The currently loaded track's file key (empty when nothing is loaded)
        // and whether it is actively playing — lets the tracklist render the
        // live VuMeter indicator without reading the facade directly.
        val playingTrackFileKey: String = "",
        val isPlaying: Boolean = false,
    ) : AlbumDetailContentState

    data class Error(val message: String) : AlbumDetailContentState
}

data class AlbumDetailViewState(
    val albumName: String,
    val artistName: String,
    val contentState: AlbumDetailContentState = AlbumDetailContentState.Loading,
    val artistInfoDialogArtist: String? = null,
    val artistInfoDialogState: ArtistInfoState = ArtistInfoState.Idle,
    val isOfflineMode: Boolean = true,
    val transientError: String? = null,
)

class AlbumDetailViewModel(
    val album: Album,
    private val libraryRepository: LibraryRepository,
    private val facade: AudioPlayerFacade,
    private val database: JrrDatabase,
    private val artistInfoRepository: ArtistInfoRepository? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(AlbumDetailViewState(album.name, album.albumArtist))
    val state: StateFlow<AlbumDetailViewState> = _state.asStateFlow()

    // Internal state updates
    private val tracksFlow = MutableStateFlow<List<Track>>(emptyList())
    private val favoriteFlow = MutableStateFlow(false)

    init {
        log.d { "init album=${album.name} artist=${album.albumArtist}" }
        state.logged(log, "state") { it.summary() }.launchIn(viewModelScope)
        // Favorites are per real server: re-subscribe on identity change.
        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        val favoritesFlow = facade.activeServerId.flatMapLatest { sid ->
            database.favoriteDao().getAllFavoritesFlow(sid)
        }
        val dbStateFlow = combine(
            database.downloadedTrackDao().getAllTracksFlow(),
            database.downloadJobDao().getAllJobsFlow(),
            favoritesFlow
        ) { downloaded, jobs, favorites ->
            Triple(downloaded, jobs, favorites)
        }

        // Folded so the outer combine stays within the 5-flow typed overloads.
        val playbackFlow = combine(
            facade.activeZone,
            facade.playCounts,
            facade.playerStatus
        ) { activeZone, playCounts, playerStatus ->
            Triple(activeZone, playCounts, playerStatus)
        }

        combine(
            tracksFlow,
            dbStateFlow,
            favoriteFlow,
            playbackFlow
        ) { tracks, dbState, favorite, playback ->
            val (downloaded, jobs, favorites) = dbState
            val (activeZone, playCounts, playerStatus) = playback
            val downloadedKeys = downloaded.map { it.fileKey }.toSet()
            val activeJobs = jobs.associate { it.fileKey to it.state }
            val favoritedTrackKeys = favorites.filter { it.type == "track" }.map { it.identifier }.toSet()
            val isOffline = activeZone.isOffline || facade.currentServerHost.isNullOrEmpty()
            // Overlay live server play counts so the played icon reflects the
            // authoritative [Number Plays] without leaving/re-fetching the screen.
            val livePlayTracks = tracks.overlayPlayCounts(playCounts)
            val headerArtworkUrl = facade.artworkUrl(livePlayTracks.firstOrNull()?.fileKey ?: "")

            _state.update { currentState ->
                // Don't overwrite an error state with fresh data — the user
                // needs to retry first.
                if (currentState.contentState is AlbumDetailContentState.Error) {
                    currentState.copy(isOfflineMode = isOffline)
                } else {
                    currentState.copy(
                        contentState = AlbumDetailContentState.Success(
                            tracks = livePlayTracks,
                            downloadedTrackKeys = downloadedKeys,
                            activeDownloadJobs = activeJobs,
                            isFavorite = favorite,
                            favoritedTrackKeys = favoritedTrackKeys,
                            headerArtworkUrl = headerArtworkUrl,
                            playingTrackFileKey = playerStatus?.trackFileKey.orEmpty(),
                            isPlaying = playerStatus?.state == PlaybackState.PLAYING,
                        ),
                        isOfflineMode = isOffline,
                    )
                }
            }
        }.launchIn(viewModelScope)

        refreshTracksAndFavorite()
    }

    private fun refreshTracksAndFavorite() {
        viewModelScope.launch {
            log.d { "refreshTracksAndFavorite()" }
            _state.update { it.copy(contentState = AlbumDetailContentState.Loading) }
            try {
                // Tracks come from the network, the favorite flag from the
                // local DB — independent, so load them concurrently. If one
                // leg fails the other is cancelled and we land in the catch.
                val (albumTracks, favorite) = parZip(
                    { libraryRepository.getAlbumTracks(album) },
                    {
                        database.favoriteDao()
                            .getFavorite(facade.activeServerId.value, "album", album.albumGroupId) != null
                    },
                ) { tracks, fav -> tracks to fav }
                log.d { "loaded ${albumTracks.size} tracks (favorite=$favorite)" }
                tracksFlow.value = albumTracks
                favoriteFlow.value = favorite
            } catch (e: Exception) {
                log.e(e) { "refreshTracksAndFavorite failed" }
                _state.update {
                    it.copy(
                        contentState = AlbumDetailContentState.Error(
                            message = e.message ?: "Failed to load album tracks",
                        ),
                    )
                }
            }
        }
    }

    fun playTrack(track: Track) {
        log.d { "playTrack(${track.fileKey} / ${track.name})" }
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            val startIndex = currentTracks.indexOf(track).coerceAtLeast(0)
            facade.setQueue(currentTracks, startIndex)
            facade.play()
        }
    }

    fun playAlbum() {
        log.d { "playAlbum() tracks=${tracksFlow.value.size}" }
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            facade.setQueue(currentTracks, 0)
            facade.play()
        }
    }

    fun shuffleAlbum() {
        log.d { "shuffleAlbum() tracks=${tracksFlow.value.size}" }
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            facade.setQueue(currentTracks.shuffled(), 0)
            facade.play()
        }
    }

    fun toggleFavorite() {
        log.d { "toggleFavorite()" }
        viewModelScope.launch {
            try {
                val identifier = album.albumGroupId
                val dao = database.favoriteDao()
                val sid = facade.activeServerId.value
                val existing = dao.getFavorite(sid, "album", identifier)
                if (existing != null) {
                    dao.delete(existing)
                    favoriteFlow.value = false
                    log.d { "favorite removed identifier=$identifier" }
                } else {
                    val displayName = "${album.name}|${album.albumArtist}|${album.folderPath}|${album.parentFolderPath}|${album.date}|${album.artworkFileKey}|${album.totalDiscs}|${album.discNumber}"
                    val newFav = FavoriteEntity(
                        serverId = sid,
                        type = "album",
                        identifier = identifier,
                        displayName = displayName,
                        addedAt = getTimeMillis()
                    )
                    dao.insert(newFav)
                    favoriteFlow.value = true
                    log.d { "favorite added identifier=$identifier" }
                }
            } catch (e: Exception) {
                log.e(e) { "toggleFavorite failed" }
                _state.update { it.copy(transientError = "Failed to toggle favorite: ${e.message ?: "unknown error"}") }
            }
        }
    }
    fun toggleFavoriteTrack(track: Track) {
        log.d { "toggleFavoriteTrack(fileKey=${track.fileKey})" }
        viewModelScope.launch {
            try {
                val dao = database.favoriteDao()
                val sid = facade.activeServerId.value
                val existing = dao.getFavorite(sid, "track", track.fileKey)
                if (existing != null) {
                    dao.delete(existing)
                    log.d { "favorite track removed fileKey=${track.fileKey}" }
                } else {
                    val displayName = "${track.name}|${track.artist}|${track.album}|${track.durationMs}"
                    val newFav = FavoriteEntity(
                        serverId = sid,
                        type = "track",
                        identifier = track.fileKey,
                        displayName = displayName,
                        addedAt = getTimeMillis()
                    )
                    dao.insert(newFav)
                    log.d { "favorite track added fileKey=${track.fileKey}" }
                }
            } catch (e: Exception) {
                log.e(e) { "toggleFavoriteTrack failed" }
                _state.update { it.copy(transientError = "Failed to toggle track favorite: ${e.message ?: "unknown error"}") }
            }
        }
    }
    fun startDownload(track: Track) {
        log.d { "startDownload(${track.fileKey} / ${track.name})" }
        viewModelScope.launch {
            try {
                libraryRepository.startDownload(track)
            } catch (e: Exception) {
                log.e(e) { "startDownload failed fileKey=${track.fileKey}" }
                _state.update { it.copy(transientError = "Failed to start download: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun addTrackToQueue(track: Track) {
        log.d { "addTrackToQueue(${track.fileKey} / ${track.name})" }
        facade.addTracks(listOf(track))
    }

    fun playTrackNext(track: Track) {
        log.d { "playTrackNext(${track.fileKey} / ${track.name})" }
        facade.playNextTracks(listOf(track))
    }

    fun showArtistInfoForTrack(track: Track) {
        val artist = track.artist.ifBlank { album.albumArtist }
        log.d { "showArtistInfoForTrack(fileKey=${track.fileKey}, artist=$artist)" }
        if (artist.isBlank()) {
            _state.update {
                it.copy(
                    artistInfoDialogArtist = "Unknown artist",
                    artistInfoDialogState = ArtistInfoState.Error("Track artist is missing"),
                )
            }
            return
        }
        showArtistInfoForArtist(artist)
    }

    fun reloadArtistInfoDialog() {
        _state.value.artistInfoDialogArtist?.let { artist ->
            if (artist != "Unknown artist") showArtistInfoForArtist(artist)
        }
    }

    private fun showArtistInfoForArtist(artist: String) {
        val repository = artistInfoRepository
        if (repository == null) {
            _state.update {
                it.copy(
                    artistInfoDialogArtist = artist,
                    artistInfoDialogState = ArtistInfoState.Error("AI artist info is not available on this platform"),
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    artistInfoDialogArtist = artist,
                    artistInfoDialogState = ArtistInfoState.Loading,
                )
            }
            try {
                val info = repository.getArtistInfo(artist)
                _state.update {
                    it.copy(
                        artistInfoDialogArtist = artist,
                        artistInfoDialogState = ArtistInfoState.Success(info),
                    )
                }
            } catch (e: Exception) {
                log.e(e) { "showArtistInfoForTrack failed artist=$artist" }
                _state.update {
                    it.copy(
                        artistInfoDialogArtist = artist,
                        artistInfoDialogState = ArtistInfoState.Error(e.message ?: "Failed to load artist info"),
                    )
                }
            }
        }
    }

    fun dismissArtistInfoDialog() {
        log.d { "dismissArtistInfoDialog()" }
        _state.update {
            it.copy(
                artistInfoDialogArtist = null,
                artistInfoDialogState = ArtistInfoState.Idle,
            )
        }
    }

    fun addAlbumToQueue() {
        log.d { "addAlbumToQueue() tracks=${tracksFlow.value.size}" }
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            facade.addTracks(currentTracks)
        }
    }

    fun playAlbumNext() {
        log.d { "playAlbumNext() tracks=${tracksFlow.value.size}" }
        val currentTracks = tracksFlow.value
        if (currentTracks.isNotEmpty()) {
            facade.playNextTracks(currentTracks)
        }
    }

    fun downloadAlbum() {
        log.d { "downloadAlbum() tracks=${tracksFlow.value.size}" }
        viewModelScope.launch {
            try {
                val currentTracks = tracksFlow.value
                currentTracks.forEach { libraryRepository.startDownload(it) }
            } catch (e: Exception) {
                log.e(e) { "downloadAlbum failed" }
                _state.update { it.copy(transientError = "Download failed: ${e.message ?: "unknown error"}") }
            }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }

    fun retry() {
        log.d { "retry()" }
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
        log.d { "dispose album=${album.name}" }
        viewModelScope.cancel()
    }
}
