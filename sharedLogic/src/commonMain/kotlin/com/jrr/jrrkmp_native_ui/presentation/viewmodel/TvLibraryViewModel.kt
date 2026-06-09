package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.api.BrowseItem
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
import com.jrr.jrrkmp_native_ui.data.db.entity.FavoriteEntity
import io.ktor.util.date.getTimeMillis

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

    /** Active real-server identity favorites are scoped to (empty if none). */
    private val sid: String get() = facade.activeServerId.value

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

    /** Favorited albums, reconstructed from the stored 8-part metadata or legacy name|albumArtist id. */
    suspend fun favoriteAlbums(): List<Album> = withContext(Dispatchers.IO) {
        log.d { "favoriteAlbums()" }
        database.favoriteDao().getAllFavorites(sid)
            .filter { it.type == "album" }
            .map { fav ->
                val parts = fav.displayName.split("|")
                if (parts.size >= 8) {
                    Album(
                        name = parts[0],
                        albumArtist = parts[1],
                        folderPath = parts[2],
                        parentFolderPath = parts[3],
                        date = parts[4],
                        artworkFileKey = parts[5],
                        totalDiscs = parts[6].toIntOrNull() ?: 1,
                        discNumber = parts[7].toIntOrNull() ?: 1,
                    )
                } else {
                    val oldParts = fav.identifier.split("|", limit = 2)
                    Album(
                        name = oldParts.getOrElse(0) { fav.displayName },
                        albumArtist = oldParts.getOrElse(1) { "Unknown Artist" },
                        folderPath = "",
                        parentFolderPath = "",
                        date = "",
                        artworkFileKey = "",
                        totalDiscs = 1,
                        discNumber = 1,
                    )
                }
            }
    }

    /** Group a flat track list by Album Artist → Album (reuses the shared,
     *  multi-disc-aware grouping). Used by the Browse leaf's grouped view. */
    fun group(tracks: List<Track>): List<ArtistTrackGroup> = groupTracksByArtistAndAlbum(tracks)

    /** Tracks never played (Number Plays unset/zero) — Browse "Show not played". */
    fun notPlayed(tracks: List<Track>): List<Track> = libraryRepository.notPlayedTracks(tracks)

    /** Deterministically shuffled track list for the given seed — Browse "Shuffle". */
    fun shuffle(tracks: List<Track>, seed: Long): List<Track> =
        libraryRepository.shuffleTracks(tracks, seed)

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

    /** Add [tracks] to play next in the queue. */
    fun playNext(tracks: List<Track>) {
        log.d { "playNext(${tracks.size} tracks)" }
        try {
            facade.playNextTracks(tracks)
        } catch (e: Exception) {
            log.e(e) { "playNext failed" }
        }
    }

    /** Append [tracks] to the end of the queue. */
    fun addTracksToQueue(tracks: List<Track>) {
        log.d { "addTracksToQueue(${tracks.size} tracks)" }
        try {
            facade.addTracks(tracks)
        } catch (e: Exception) {
            log.e(e) { "addTracksToQueue failed" }
        }
    }

    /** Browse the files under a node. */
    suspend fun browseFiles(nodeId: String): List<Track> {
        log.d { "browseFiles(nodeId=$nodeId)" }
        return libraryRepository.getBrowseFiles(nodeId)
    }

    /** Browse the children of a node. */
    suspend fun browseChildren(parentId: String): List<BrowseItem> {
        log.d { "browseChildren(parentId=$parentId)" }
        return libraryRepository.getBrowseChildren(parentId)
    }

    /** Favorited playlists/browse nodes. */
    suspend fun favoritePlaylists(): List<BrowseItem> = withContext(Dispatchers.IO) {
        log.d { "favoritePlaylists()" }
        database.favoriteDao().getAllFavorites(sid)
            .filter { it.type == "playlist" }
            .map { fav ->
                BrowseItem(key = fav.identifier, name = fav.displayName)
            }
    }

    suspend fun isPlaylistFavorite(key: String): Boolean = withContext(Dispatchers.IO) {
        log.d { "isPlaylistFavorite(key=$key)" }
        database.favoriteDao().getFavorite(sid, "playlist", key) != null
    }

    suspend fun togglePlaylistFavorite(key: String, name: String): Boolean = withContext(Dispatchers.IO) {
        log.d { "togglePlaylistFavorite(key=$key, name=$name)" }
        val dao = database.favoriteDao()
        val existing = dao.getFavorite(sid, "playlist", key)
        if (existing != null) {
            dao.delete(existing)
            false
        } else {
            val newFav = FavoriteEntity(
                serverId = sid,
                type = "playlist",
                identifier = key,
                displayName = name,
                addedAt = getTimeMillis()
            )
            dao.insert(newFav)
            true
        }
    }

    suspend fun favoriteTracks(): List<Track> = withContext(Dispatchers.IO) {
        log.d { "favoriteTracks()" }
        database.favoriteDao().getAllFavorites(sid)
            .filter { it.type == "track" }
            .map { fav ->
                val parts = fav.displayName.split("|")
                val name = parts.getOrNull(0) ?: ""
                val artist = parts.getOrNull(1) ?: ""
                val album = parts.getOrNull(2) ?: ""
                val durationMs = parts.getOrNull(3)?.toLongOrNull() ?: 0L
                Track(
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
            }
    }

    suspend fun isTrackFavorite(fileKey: String): Boolean = withContext(Dispatchers.IO) {
        log.d { "isTrackFavorite(fileKey=$fileKey)" }
        database.favoriteDao().getFavorite(sid, "track", fileKey) != null
    }

    suspend fun toggleTrackFavorite(track: Track): Boolean = withContext(Dispatchers.IO) {
        log.d { "toggleTrackFavorite(fileKey=${track.fileKey})" }
        val dao = database.favoriteDao()
        val existing = dao.getFavorite(sid, "track", track.fileKey)
        if (existing != null) {
            dao.delete(existing)
            false
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
            true
        }
    }

    suspend fun isAlbumFavorite(albumGroupId: String): Boolean = withContext(Dispatchers.IO) {
        log.d { "isAlbumFavorite(albumGroupId=$albumGroupId)" }
        database.favoriteDao().getFavorite(sid, "album", albumGroupId) != null
    }

    suspend fun toggleAlbumFavorite(album: Album): Boolean = withContext(Dispatchers.IO) {
        log.d { "toggleAlbumFavorite(albumGroupId=${album.albumGroupId})" }
        val dao = database.favoriteDao()
        val identifier = album.albumGroupId
        val existing = dao.getFavorite(sid, "album", identifier)
        if (existing != null) {
            dao.delete(existing)
            false
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
            true
        }
    }
}
