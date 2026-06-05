package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.repository.BrowseContent
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.groupTracksByArtistAndAlbum
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.ArtistTrackGroup
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private val log = Logger.withTag("vm:TvLibrary")

/**
 * Backs the Android TV Library sections (Artists / Random Albums / Browse /
 * Favorites) and their track lists. It exists so the TV composables never touch
 * the repository, DB, [McwsClient] or [AudioPlayerFacade] directly — per the
 * ViewModel architecture, all feature data access and playback go through here.
 * The TV's Compose drill-down navigation drives these loaders/actions.
 */
class TvLibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val facade: AudioPlayerFacade,
    private val database: JrrDatabase,
    private val mcwsClient: McwsClient,
) : ViewModel() {

    init {
        log.d { "init" }
    }

    suspend fun artists(): List<String> {
        log.d { "artists()" }
        return libraryRepository.getArtists()
    }

    suspend fun albums(artist: String): List<Album> {
        log.d { "albums(artist=$artist)" }
        return libraryRepository.getAlbumsByArtist(artist)
    }

    suspend fun randomAlbums(limit: Int = 24): List<Album> {
        log.d { "randomAlbums(limit=$limit)" }
        return libraryRepository.getRandomAlbums(limit)
    }

    suspend fun albumTracks(album: Album): List<Track> {
        log.d { "albumTracks(${album.name})" }
        return libraryRepository.getAlbumTracks(album)
    }

    suspend fun browseNode(nodeId: String): BrowseContent {
        log.d { "browseNode(nodeId=$nodeId)" }
        return libraryRepository.getBrowseNode(nodeId)
    }

    /** Favorited albums, reconstructed from the stored `name|albumArtist` id. */
    suspend fun favoriteAlbums(): List<Album> = withContext(Dispatchers.IO) {
        log.d { "favoriteAlbums()" }
        database.favoriteDao().getAllFavorites()
            .filter { it.type == "album" }
            .map { fav ->
                val parts = fav.identifier.split("|", limit = 2)
                Album(
                    name = parts.getOrElse(0) { fav.displayName },
                    albumArtist = parts.getOrElse(1) { "Unknown Artist" },
                    folderPath = "",
                    parentFolderPath = "",
                    date = "",
                    artworkFileKey = "",
                    totalDiscs = 1,
                    discNumber = 1,
                )
            }
    }

    /** Group a flat track list by Album Artist → Album (reuses the shared,
     *  multi-disc-aware grouping). Used by the Browse leaf's grouped view. */
    fun group(tracks: List<Track>): List<ArtistTrackGroup> = groupTracksByArtistAndAlbum(tracks)

    /** Thumbnail URL for an artwork file key, or null when there is none. */
    fun artworkUrl(fileKey: String): String? =
        fileKey.takeIf { it.isNotEmpty() }?.let { mcwsClient.buildImageUrl(it) }

    /** Replace the queue with [tracks] starting at [startIndex] and play. */
    fun play(tracks: List<Track>, startIndex: Int) {
        log.d { "play(${tracks.size} tracks, startIndex=$startIndex)" }
        try {
            facade.setQueue(tracks, startIndex)
            facade.play()
        } catch (e: Exception) {
            log.e(e) { "play failed" }
        }
    }
}
