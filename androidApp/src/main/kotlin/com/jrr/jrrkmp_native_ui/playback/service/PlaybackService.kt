package com.jrr.jrrkmp_native_ui.playback.service

import android.app.PendingIntent
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Album
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlinx.serialization.encodeToString
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueTrackEntity
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueStateEntity

private val log = Logger.withTag("playback:CarService")

private val json = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

private fun normalizeForMatch(s: String): String {
    return s.replace(Regex("[^a-zA-Z0-9]"), "").lowercase()
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class PlaybackService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val currentSearchResults = ConcurrentHashMap<String, List<MediaItem>>()

    // Snapshot of the current "Random Albums" set. Paginated onGetChildren calls
    // read this so all pages of one render stay consistent, while each refresh
    // (startup, reconnect, "Refresh Library" action) rolls a new random set.
    @Volatile
    private var randomAlbumsSnapshot: List<MediaItem> = emptyList()

    override fun onCreate() {
        super.onCreate()
        val facade = this.appContainer.facade
        val player = this.appContainer.localPlayerHandler.getUnderlyingPlayer()

        val sessionActivityPendingIntent = packageManager.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(
                this,
                0,
                sessionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            player,
            LibraryCallback()
        ).apply {
            if (sessionActivityPendingIntent != null) {
                setSessionActivity(sessionActivityPendingIntent)
            }
        }.build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        val isCar = controllerInfo.packageName == "com.google.android.projection.gearhead" ||
                controllerInfo.packageName == "com.google.android.car.assistant" ||
                controllerInfo.packageName.contains("automotive", ignoreCase = true) ||
                controllerInfo.connectionHints.containsKey("androidx.media3.session.connection.CAR_CONNECTION") ||
                controllerInfo.connectionHints.getBoolean("androidx.media3.session.connection.CAR_CONNECTION", false)
        if (isCar) {
            val facade = this.appContainer.facade
            val currentZone = facade.activeZone.value
            if (!currentZone.isLocal && !currentZone.isOffline) {
                val hasConnection = !facade.currentServerHost.isNullOrEmpty()
                val targetZone = if (hasConnection) Zone.Local else Zone.Offline
                facade.setZone(targetZone)
            }
        }
        return mediaLibrarySession
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaLibrarySession?.run {
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    private suspend fun getTrackByKey(mediaId: String): Track? {
        if (mediaId.isEmpty() || !mediaId.all { it.isDigit() }) {
            return null
        }
        val db = this@PlaybackService.appContainer.database
        val localTrack = db.downloadedTrackDao().getTrack(mediaId)?.toTrack()
        if (localTrack != null) {
            return localTrack
        }
        val facade = this@PlaybackService.appContainer.facade
        val isOffline = facade.currentServerHost.isNullOrEmpty()
        if (!isOffline) {
            try {
                val mcwsClient = this@PlaybackService.appContainer.mcwsClient
                val onlineTracks = mcwsClient.searchTracks("[Key]=[$mediaId]")
                return onlineTracks.firstOrNull()
            } catch (e: Exception) {
                log.w(e) { "getTrackByKey($mediaId): online lookup failed" }
            }
        }
        return null
    }

    private fun mapTrackToMediaItem(track: Track): MediaItem =
        mapDomainTrackToMediaItem(track)

    private fun mapTracksToMediaItems(tracks: List<Track>): List<MediaItem> =
        tracks.map { mapDomainTrackToMediaItem(it) }

    /**
     * Roll a fresh random-albums set and broadcast a children-changed signal to
     * every connected controller so Android Auto re-queries the node. Uses the
     * broadcast overload (not a captured ControllerInfo, which goes stale across
     * AA's controller/service churn).
     */
    private fun refreshRandomAlbums(reason: String) {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val repository = this@PlaybackService.appContainer.libraryRepository
                val albums = repository.getRandomAlbums(limit = 50)
                randomAlbumsSnapshot = mapAlbumsToBrowsableItems(albums)
                log.d { "random refreshed ($reason): ${albums.take(3).map { it.name }}" }
                withContext(Dispatchers.Main) {
                    mediaLibrarySession?.notifyChildrenChanged(
                        "random_albums", randomAlbumsSnapshot.size, null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.w(e) { "random refresh failed ($reason)" }
            }
        }
    }

    /**
     * Artwork URI for a file key: prefer the downloaded copy (instant, offline),
     * otherwise fall back to the MCWS thumbnail (via the facade, active server
     * only) so remote albums/tracks still show art in Android Auto. Media3's
     * default BitmapLoader fetches the http(s) URI for us.
     */
    private fun artworkUriFor(fileKey: String): Uri {
        val localArt = File(filesDir, "downloads/art_$fileKey.jpg")
        if (localArt.exists()) {
            return Uri.parse(
                "content://com.jrr.jrrkmp_native_ui.fileprovider/downloads/art_$fileKey.jpg"
            )
        }
        val remote = this@PlaybackService.appContainer.facade.artworkUrl(fileKey)
        if (remote.isNotEmpty()) {
            return Uri.parse(remote)
        }
        return Uri.parse(
            "content://com.jrr.jrrkmp_native_ui.fileprovider/downloads/art_$fileKey.jpg"
        )
    }

    /**
     * Artwork URI for a Browse-tree node (album/category folder). JRiver renders
     * the representative cover for a browse node via `Browse/Image?ID=<nodeId>`,
     * which is different from a file's `File/GetImage`. Returns null when we have
     * no server context.
     */
    private fun browseNodeArtUri(nodeId: String): Uri? {
        if (nodeId.isEmpty()) return null
        val url = this@PlaybackService.appContainer.facade.browseNodeArtUrl(nodeId)
        return if (url.isEmpty()) null else Uri.parse(url)
    }

    /** JRiver "Album Artist (auto)": album artist, falling back to track artist. */
    private fun autoAlbumArtist(track: Track): String =
        track.albumArtist.ifBlank { track.artist }.ifBlank { "Unknown Artist" }

    private fun albumOf(track: Track): String = track.album.ifBlank { "Unknown Album" }

    /** Downloaded tracks for one (album-artist, album) group, in disc/track order. */
    private suspend fun downloadedAlbumTracks(artist: String, album: String): List<Track> =
        this@PlaybackService.appContainer.libraryRepository.getDownloadedTracks()
            .filter { autoAlbumArtist(it) == artist && albumOf(it) == album }
            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))

    /** Browsable album rows keyed `album|<artist>|<name>` (matches the Albums node). */
    private fun mapAlbumsToBrowsableItems(albums: List<Album>): List<MediaItem> {
        return albums.map { album ->
            MediaItem.Builder()
                .setMediaId("album|${album.albumArtist}|${album.name}")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(album.name)
                        .setSubtitle(album.albumArtist)
                        .setArtworkUri(artworkUriFor(album.artworkFileKey))
                        .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                        .setIsPlayable(true)
                        .setIsBrowsable(true)
                        .build()
                )
                .build()
        }
    }

    private fun mapDomainTrackToMediaItem(track: Track): MediaItem {
        val db = this@PlaybackService.appContainer.database
        val localTrack = runBlocking { db.downloadedTrackDao().getTrack(track.fileKey) }
        val uri = if (localTrack != null && File(localTrack.filePath).exists()) {
            Uri.fromFile(File(localTrack.filePath))
        } else {
            // Stream URL (server + quality + Channels=2) comes from the facade —
            // single source of truth, active server only.
            Uri.parse(this@PlaybackService.appContainer.facade.streamUrl(track.fileKey, playback = true))
        }
        val artUri = artworkUriFor(track.fileKey)
        val extras = android.os.Bundle().apply {
            putString("track_json", json.encodeToString(track))
        }
        return MediaItem.Builder()
            .setMediaId(track.fileKey)
            .setUri(uri)
            .setTag(track)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(artUri)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .setExtras(extras)
                    .build()
            )
            .build()
    }

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId("root_id")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                        .setIsPlayable(false)
                        .setIsBrowsable(true)
                        .setTitle("Jrr Root")
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            log.v { "onSubscribe(parentId=$parentId)" }
            // Only the random-albums node needs a forced refresh on (re)open.
            // Generate a fresh snapshot ONCE here, then notify so the browser
            // re-pages over that stable snapshot — otherwise each page request
            // would re-roll a different random set and the list would be
            // incoherent. The notify must run after this callback returns and
            // the subscription is registered, so post it.
            if (parentId == "random_albums") {
                refreshRandomAlbums("onSubscribe")
            }
            return Futures.immediateFuture(LibraryResult.ofVoid())
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val repository = this@PlaybackService.appContainer.libraryRepository
                    val resultList = mutableListOf<MediaItem>()

                    if (parentId == "root_id") {
                        resultList.add(
                            MediaItem.Builder()
                                .setMediaId("downloads")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Downloads")
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_TITLES)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        )
                        resultList.add(
                            MediaItem.Builder()
                                .setMediaId("random_albums")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Random Albums")
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        )
                        resultList.add(
                            MediaItem.Builder()
                                .setMediaId("artists")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Artists")
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ARTISTS)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        )
                        resultList.add(
                            MediaItem.Builder()
                                .setMediaId("browse|-1")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Browse")
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        )
                        resultList.add(
                            MediaItem.Builder()
                                .setMediaId("refresh_library")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Refresh Library")
                                        .setSubtitle("Update Random Albums")
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_TITLES)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        )
                    } else if (parentId == "refresh_library") {
                        // User tapped "Refresh Library": re-roll the dynamic
                        // categories and tell AA to re-query them, then show a
                        // confirmation node (matches the Flutter app's flow).
                        refreshRandomAlbums("manual refresh")
                        resultList.add(
                            MediaItem.Builder()
                                .setMediaId("refresh_done")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Refresh complete")
                                        .setSubtitle("Go back to see updated Random Albums")
                                        .setIsPlayable(false)
                                        .setIsBrowsable(false)
                                        .build()
                                )
                                .build()
                        )
                    } else if (parentId == "downloads") {
                        // Group downloaded tracks by Album Artist (auto).
                        val artists = repository.getDownloadedTracks()
                            .map { autoAlbumArtist(it) }
                            .distinct()
                            .sortedBy { it.lowercase() }
                        resultList.addAll(artists.map { artist ->
                            MediaItem.Builder()
                                .setMediaId("dl_artist|$artist")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(artist)
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ARTISTS)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("dl_artist|")) {
                        // Albums for one album-artist, drawn from downloads.
                        val artist = parentId.substringAfter("dl_artist|")
                        val albums = repository.getDownloadedTracks()
                            .filter { autoAlbumArtist(it) == artist }
                            .groupBy { albumOf(it) }
                            .entries
                            .sortedBy { it.key.lowercase() }
                        resultList.addAll(albums.map { (album, albumTracks) ->
                            val artKey = albumTracks.firstOrNull()?.fileKey ?: ""
                            MediaItem.Builder()
                                .setMediaId("dl_album|$artist|$album")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(album)
                                        .setSubtitle(artist)
                                        .setArtworkUri(artworkUriFor(artKey))
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                        .setIsPlayable(true)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("dl_album|")) {
                        val parts = parentId.split("|")
                        val artist = parts.getOrNull(1) ?: ""
                        val album = parts.getOrNull(2) ?: ""
                        val tracks = downloadedAlbumTracks(artist, album)
                        if (tracks.isNotEmpty()) {
                            resultList.add(
                                MediaItem.Builder()
                                    .setMediaId("dl_play|$artist|$album")
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle("Play All")
                                            .setIsPlayable(true)
                                            .setIsBrowsable(false)
                                            .build()
                                    )
                                    .build()
                            )
                            resultList.addAll(mapTracksToMediaItems(tracks))
                        }
                    } else if (parentId == "random_albums") {
                        // Page over the snapshot generated in onSubscribe so all
                        // pages of one open are consistent. Fall back to a fresh
                        // fetch if the snapshot isn't ready yet (first paint).
                        val snapshot = randomAlbumsSnapshot.ifEmpty {
                            mapAlbumsToBrowsableItems(repository.getRandomAlbums(limit = 50))
                                .also { randomAlbumsSnapshot = it }
                        }
                        log.v { "random_albums page=$page size=${snapshot.size}" }
                        resultList.addAll(snapshot)
                    } else if (parentId == "artists") {
                        val artists = repository.getArtists()
                        resultList.addAll(artists.map { artist ->
                            MediaItem.Builder()
                                .setMediaId("artist|$artist")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(artist)
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ARTISTS)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("artist|")) {
                        val artistName = parentId.substring("artist|".length)
                        val albums = repository.getAlbumsByArtist(artistName)
                        resultList.addAll(albums.map { album ->
                            MediaItem.Builder()
                                .setMediaId("artist_album|$artistName|${album.name}")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(album.name)
                                        .setArtworkUri(artworkUriFor(album.artworkFileKey))
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                        .setIsPlayable(true)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("artist_album|")) {
                        val parts = parentId.split("|")
                        val artistName = parts.getOrNull(1) ?: ""
                        val albumName = parts.getOrNull(2) ?: ""
                        val albums = repository.getAlbumsByArtist(artistName)
                        val album = albums.find { it.name.equals(albumName, ignoreCase = true) }
                        if (album != null) {
                            resultList.add(
                                MediaItem.Builder()
                                    .setMediaId("play_album|$artistName|$albumName")
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle("Play All")
                                            .setIsPlayable(true)
                                            .setIsBrowsable(false)
                                            .build()
                                    )
                                    .build()
                            )
                            val tracks = repository.getAlbumTracks(album)
                            resultList.addAll(mapTracksToMediaItems(tracks))
                        }
                    } else if (parentId.startsWith("browse|")) {
                        // Mirror the library's MCWS Browse hierarchy. A node with
                        // child categories is a folder; one with only files is a
                        // leaf — opening a leaf starts playback of its tracks.
                        val nodeId = parentId.substringAfter("browse|")
                        val children = repository.getBrowseChildren(nodeId)
                        if (children.isNotEmpty()) {
                            resultList.addAll(children.map { item ->
                                MediaItem.Builder()
                                    .setMediaId("browse|${item.key}")
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(item.name)
                                            .setArtworkUri(browseNodeArtUri(item.key))
                                            .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                                            .setIsPlayable(false)
                                            .setIsBrowsable(true)
                                            .build()
                                    )
                                    .build()
                            })
                        } else {
                            val tracks = repository.getBrowseFiles(nodeId)
                            if (tracks.isNotEmpty()) {
                                // Only fire playback on the first page request,
                                // not on each pagination call for the same leaf.
                                if (page == 0) {
                                    log.d { "browse leaf opened (node=$nodeId): playing ${tracks.size} tracks" }
                                    val facade = this@PlaybackService.appContainer.facade
                                    withContext(Dispatchers.Main) {
                                        val hasConnection = !facade.currentServerHost.isNullOrEmpty()
                                        val targetZone = if (hasConnection) Zone.Local else Zone.Offline
                                        if (facade.activeZone.value.id != targetZone.id) {
                                            facade.setZone(targetZone, skipLoadQueue = true)
                                        }
                                        facade.setQueue(tracks, 0)
                                    }
                                }
                                resultList.addAll(mapTracksToMediaItems(tracks))
                            }
                        }
                    } else if (parentId.startsWith("album|")) {
                        val parts = parentId.split("|")
                        val artistName = parts.getOrNull(1) ?: ""
                        val albumName = parts.getOrNull(2) ?: ""
                        val albums = repository.getAllAlbums()
                        val album = albums.find {
                            it.name.equals(albumName, ignoreCase = true) &&
                                    it.albumArtist.equals(artistName, ignoreCase = true)
                        }
                        if (album != null) {
                            resultList.add(
                                MediaItem.Builder()
                                    .setMediaId("play_album|$artistName|$albumName")
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle("Play All")
                                            .setIsPlayable(true)
                                            .setIsBrowsable(false)
                                            .build()
                                    )
                                    .build()
                            )
                            val tracks = repository.getAlbumTracks(album)
                            resultList.addAll(mapTracksToMediaItems(tracks))
                        }
                    }

                    val pageStart = page * pageSize
                    val pageEnd = minOf(pageStart + pageSize, resultList.size)
                    val pagedList = if (pageStart < resultList.size) {
                        resultList.subList(pageStart, pageEnd)
                    } else {
                        emptyList()
                    }

                    future.set(LibraryResult.ofItemList(ImmutableList.copyOf(pagedList), params))
                } catch (e: Exception) {
                    log.e(e) { "media library callback failed" }
                    future.setException(e)
                }
            }
            return future
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val future = SettableFuture.create<LibraryResult<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val track = getTrackByKey(mediaId)
                    if (track != null) {
                        future.set(LibraryResult.ofItem(mapTrackToMediaItem(track), null))
                    } else {
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                } catch (e: Exception) {
                    log.e(e) { "media library callback failed" }
                    future.setException(e)
                }
            }
            return future
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            val future = SettableFuture.create<LibraryResult<Void>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val db = this@PlaybackService.appContainer.database
                    val allTracks = db.downloadedTrackDao().getAllTracks()
                    val resolver = VoiceSearchResolver()
                    val searchResult = resolver.resolve(query, null, allTracks)

                    currentSearchResults[query] = mapTracksToMediaItems(searchResult.tracks.map { it.toTrack() })

                    session.notifySearchResultChanged(browser, query, searchResult.tracks.size, params)
                    future.set(LibraryResult.ofVoid())
                } catch (e: Exception) {
                    log.e(e) { "media library callback failed" }
                    future.setException(e)
                }
            }
            return future
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
            val list = currentSearchResults[query] ?: emptyList()
            val pageStart = page * pageSize
            val pageEnd = minOf(pageStart + pageSize, list.size)
            val pagedList = if (pageStart < list.size) {
                list.subList(pageStart, pageEnd)
            } else {
                emptyList()
            }
            future.set(LibraryResult.ofItemList(ImmutableList.copyOf(pagedList), params))
            return future
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            val firstItem = mediaItems.firstOrNull()
            val query = firstItem?.requestMetadata?.searchQuery
            val extras = firstItem?.requestMetadata?.extras

            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val repository = this@PlaybackService.appContainer.libraryRepository
                    val facade = this@PlaybackService.appContainer.facade

                    withContext(Dispatchers.Main) {
                        val hasConnection = !facade.currentServerHost.isNullOrEmpty()
                        val targetZone = if (hasConnection) Zone.Local else Zone.Offline
                        if (facade.activeZone.value.id != targetZone.id) {
                            facade.setZone(targetZone, skipLoadQueue = true)
                        }
                    }

                    val isVoiceSearch = query != null || (extras != null && extras.containsKey("android.intent.extra.focus"))
                    if (isVoiceSearch) {
                        val db = this@PlaybackService.appContainer.database
                        val allTracks = db.downloadedTrackDao().getAllTracks()
                        val resolver = VoiceSearchResolver()
                        val searchResult = resolver.resolve(query, extras, allTracks)
                        val matchedTracks = searchResult.tracks

                        if (matchedTracks.isNotEmpty()) {
                            val items = mapTracksToMediaItems(matchedTracks.map { it.toTrack() })
                            withContext(Dispatchers.Main) {
                                if (searchResult.forceShuffle) {
                                    facade.setShuffleMode(com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode.ON)
                                } else {
                                    facade.setShuffleMode(com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode.OFF)
                                }
                            }
                            future.set(
                                MediaSession.MediaItemsWithStartPosition(
                                    items,
                                    if (searchResult.forceShuffle) (0 until items.size).random() else 0,
                                    0L
                                )
                            )
                        } else {
                            future.set(MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L))
                        }
                        return@launch
                    }

                    val firstItem = mediaItems.firstOrNull()
                    val mediaId = firstItem?.mediaId ?: ""

                    val queueTracks = mutableListOf<Track>()
                    var resolvedStartIndex = 0

                    if (mediaId.startsWith("dl_album|") || mediaId.startsWith("dl_play|")) {
                        val parts = mediaId.split("|")
                        val artist = parts.getOrNull(1) ?: ""
                        val album = parts.getOrNull(2) ?: ""
                        queueTracks.addAll(downloadedAlbumTracks(artist, album))
                    } else if (mediaId.startsWith("album|") || mediaId.startsWith("artist_album|") || mediaId.startsWith("play_album|")) {
                        val parts = mediaId.split("|")
                        val artistName = parts.getOrNull(1) ?: ""
                        val albumName = parts.getOrNull(2) ?: ""
                        try {
                            val albums = if (artistName.isNotEmpty()) {
                                repository.getAlbumsByArtist(artistName)
                            } else {
                                repository.getAllAlbums()
                            }
                            val album = albums.find {
                                normalizeForMatch(it.name) == normalizeForMatch(albumName) &&
                                        (artistName.isEmpty() || normalizeForMatch(it.albumArtist) == normalizeForMatch(artistName))
                            }
                            val albumTracks = album?.let { repository.getAlbumTracks(it) }
                            if (!albumTracks.isNullOrEmpty()) {
                                queueTracks.addAll(albumTracks)
                            }
                        } catch (e: Exception) {
                            log.w(e) { "onSetMediaItems: album lookup failed for $mediaId" }
                        }
                    } else {
                        val resolvedTracks = mediaItems.mapNotNull { item ->
                            getTrackByKey(item.mediaId)
                        }

                        if (resolvedTracks.isNotEmpty()) {
                            val selectedTrack = resolvedTracks.first()
                            try {
                                val albums = if (selectedTrack.albumArtist.isNotEmpty()) {
                                    repository.getAlbumsByArtist(selectedTrack.albumArtist)
                                } else {
                                    repository.getAllAlbums()
                                }
                                val album = albums.find {
                                    normalizeForMatch(it.name) == normalizeForMatch(selectedTrack.album) &&
                                            normalizeForMatch(it.albumArtist) == normalizeForMatch(selectedTrack.albumArtist)
                                }
                                val albumTracks = album?.let { repository.getAlbumTracks(it) }
                                if (!albumTracks.isNullOrEmpty()) {
                                    queueTracks.addAll(albumTracks)
                                    val idx = albumTracks.indexOfFirst { it.fileKey == selectedTrack.fileKey }
                                    if (idx >= 0) {
                                        resolvedStartIndex = idx
                                    }
                                }
                            } catch (e: Exception) {
                                log.w(e) { "onSetMediaItems: track→album lookup failed" }
                            }

                            if (queueTracks.isEmpty()) {
                                queueTracks.addAll(resolvedTracks)
                                resolvedStartIndex = 0
                            }
                        }
                    }

                    if (queueTracks.isNotEmpty()) {
                        val itemsToPlay = mapTracksToMediaItems(queueTracks)

                        // Persist the resolved queue state to the database directly
                        withContext(Dispatchers.Main) {
                            val hasConnection = !facade.currentServerHost.isNullOrEmpty()
                            val targetZone = if (hasConnection) Zone.Local else Zone.Offline
                            val db = this@PlaybackService.appContainer.database
                            serviceScope.launch(Dispatchers.IO) {
                                try {
                                    db.localQueueTrackDao().clearQueueForZone(targetZone.id)
                                    val entities = queueTracks.mapIndexed { idx, track ->
                                        LocalQueueTrackEntity(
                                            zoneId = targetZone.id,
                                            fileKey = track.fileKey,
                                            trackJson = json.encodeToString(track),
                                            position = idx
                                        )
                                    }
                                    db.localQueueTrackDao().insertAll(entities)
                                    db.localQueueStateDao().insertOrUpdate(
                                        LocalQueueStateEntity(
                                            zoneId = targetZone.id,
                                            currentIndex = resolvedStartIndex
                                        )
                                    )
                                } catch (e: Exception) {
                                    log.w(e) { "onSetMediaItems: failed to persist queue state" }
                                }
                            }
                        }

                        future.set(
                            MediaSession.MediaItemsWithStartPosition(
                                itemsToPlay,
                                resolvedStartIndex,
                                startPositionMs
                            )
                        )
                    } else {
                        val fallbackItems = mediaItems.map { item ->
                            val track = getTrackByKey(item.mediaId)
                            if (track != null) mapTrackToMediaItem(track) else item
                        }
                        future.set(
                            MediaSession.MediaItemsWithStartPosition(
                                fallbackItems,
                                startIndex,
                                startPositionMs
                            )
                        )
                    }
                } catch (e: Exception) {
                    log.e(e) { "media library callback failed" }
                    future.setException(e)
                }
            }
            return future
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {
            val future = SettableFuture.create<List<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val resolvedItems = mediaItems.map { item ->
                        val track = getTrackByKey(item.mediaId)
                        if (track != null) mapTrackToMediaItem(track) else item
                    }
                    future.set(resolvedItems)
                } catch (e: Exception) {
                    log.w(e) { "onAddMediaItems: resolve failed, passing through" }
                    future.set(mediaItems)
                }
            }
            return future
        }
    }
}
