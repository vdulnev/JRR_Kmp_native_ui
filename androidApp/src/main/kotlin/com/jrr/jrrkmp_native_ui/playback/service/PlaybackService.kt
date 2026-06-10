package com.jrr.jrrkmp_native_ui.playback.service

import android.app.PendingIntent
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import co.touchlab.kermit.Logger
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueStateEntity
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueTrackEntity
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private val log = Logger.withTag("playback:CarService")

private val json = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
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
        log.d { "onCreate" }
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
        log.d {
            "onCreate done: session built, sessionActivity=${sessionActivityPendingIntent != null} " +
                "zone=${facade.activeZone.value.id}"
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        val isCar = controllerInfo.packageName == "com.google.android.projection.gearhead" ||
                controllerInfo.packageName == "com.google.android.car.assistant" ||
                controllerInfo.packageName.contains("automotive", ignoreCase = true) ||
                controllerInfo.connectionHints.containsKey("androidx.media3.session.connection.CAR_CONNECTION") ||
                controllerInfo.connectionHints.getBoolean("androidx.media3.session.connection.CAR_CONNECTION", false)
        log.d { "onGetSession(controller=${controllerInfo.packageName}, isCar=$isCar)" }
        if (isCar) {
            val facade = this.appContainer.facade
            val currentZone = facade.activeZone.value
            if (!currentZone.isLocal && !currentZone.isOffline) {
                val hasConnection = !facade.currentServerHost.isNullOrEmpty()
                val targetZone = if (hasConnection) Zone.Local else Zone.Offline
                log.i { "car controller connected: switching zone ${currentZone.id} → ${targetZone.id}" }
                facade.setZone(targetZone)
            }
        }
        return mediaLibrarySession
    }

    override fun onDestroy() {
        log.d { "onDestroy" }
        serviceScope.cancel()
        mediaLibrarySession?.run {
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }

    private suspend fun getTrackByKey(mediaId: String): Track? {
        if (mediaId.isEmpty() || !mediaId.all { it.isDigit() }) {
            log.v { "getTrackByKey($mediaId): not a file key" }
            return null
        }
        val db = this@PlaybackService.appContainer.database
        val localTrack = db.downloadedTrackDao().getTrack(mediaId)?.toTrack()
        if (localTrack != null) {
            log.v { "getTrackByKey($mediaId): downloaded '${localTrack.name}'" }
            return localTrack
        }
        val facade = this@PlaybackService.appContainer.facade
        val isOffline = facade.currentServerHost.isNullOrEmpty()
        if (!isOffline) {
            try {
                val mcwsClient = this@PlaybackService.appContainer.mcwsClient
                val onlineTracks = mcwsClient.searchTracks("[Key]=[$mediaId]")
                log.v { "getTrackByKey($mediaId): online lookup → ${onlineTracks.firstOrNull()?.name ?: "no match"}" }
                return onlineTracks.firstOrNull()
            } catch (e: Exception) {
                log.w(e) { "getTrackByKey($mediaId): online lookup failed" }
            }
        }
        log.d { "getTrackByKey($mediaId): unresolved (offline=$isOffline)" }
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
        log.d { "refreshRandomAlbums($reason)" }
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
            log.v { "artworkUriFor($fileKey) → local file" }
            return Uri.parse(
                "content://com.jrr.jrrkmp_native_ui.fileprovider/downloads/art_$fileKey.jpg"
            )
        }
        val remote = this@PlaybackService.appContainer.facade.artworkUrl(fileKey)
        if (remote.isNotEmpty()) {
            log.v { "artworkUriFor($fileKey) → remote thumbnail" }
            return Uri.parse(remote)
        }
        log.v { "artworkUriFor($fileKey) → no art (no local file, no active server)" }
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

    /** Downloaded tracks for one (album-artist, albumGroupId) group, in
     *  disc/track order. Keyed by [Track.albumGroupId] — album names alone are
     *  not unique. */
    private suspend fun downloadedAlbumTracks(artist: String, groupId: String): List<Track> =
        this@PlaybackService.appContainer.libraryRepository.getDownloadedTracks()
            .filter { autoAlbumArtist(it) == artist && it.albumGroupId == groupId }
            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
            .also { log.d { "downloadedAlbumTracks(artist=$artist, groupId=$groupId) → ${it.size} tracks" } }

    /**
     * Browsable album rows keyed `album|<albumArtist>|<albumGroupId>`. The
     * groupId is the unique album identity (name + folder — artist/name pairs
     * are not unique); the artist scopes resolution to [getAlbumsByArtist]
     * instead of an unbounded all-albums scan. See [resolveAlbumFromMediaId].
     */
    private fun mapAlbumsToBrowsableItems(albums: List<Album>): List<MediaItem> {
        return albums.map { album ->
            MediaItem.Builder()
                .setMediaId("album|${album.albumArtist}|${album.albumGroupId}")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(album.name)
                        .setSubtitle(album.albumArtist)
                        .setArtworkUri(artworkUriFor(album.artworkFileKey))
                        .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                        .setIsPlayable(true)
                        .setIsBrowsable(true)
                        .build()
                )
                .build()
        }
    }

    /**
     * Resolve the `<albumArtist>|<albumGroupId>` payload of an `album|` /
     * `play_album|` media id. The artist is the FIRST segment only; everything
     * after it is the groupId, which itself contains '|' (name + folder path) —
     * so split once at the first '|', never fully.
     */
    private suspend fun resolveAlbumFromMediaId(payload: String): Album? {
        val artist = payload.substringBefore("|")
        val groupId = payload.substringAfter("|")
        return this@PlaybackService.appContainer.libraryRepository
            .getAlbumByGroupId(artist, groupId)
    }

    private fun mapDomainTrackToMediaItem(track: Track): MediaItem {
        val db = this@PlaybackService.appContainer.database
        val localTrack = runBlocking { db.downloadedTrackDao().getTrack(track.fileKey) }
        val uri = if (localTrack != null && File(localTrack.filePath).exists()) {
            log.v { "mapTrack(${track.fileKey} '${track.name}') → local file" }
            Uri.fromFile(File(localTrack.filePath))
        } else {
            // Stream URL (server + quality + Channels=2) comes from the facade —
            // single source of truth, active server only.
            log.v { "mapTrack(${track.fileKey} '${track.name}') → stream" }
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
            log.d { "onGetLibraryRoot(browser=${browser.packageName})" }
            val rootItem = MediaItem.Builder()
                .setMediaId("root_id")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
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
            log.d { "onSubscribe(parentId=$parentId, browser=${browser.packageName})" }
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
            log.d { "onGetChildren(parentId=$parentId, page=$page, pageSize=$pageSize, browser=${browser.packageName})" }
            val startedAtMs = System.currentTimeMillis()
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
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS)
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
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS)
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
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS)
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
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
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
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
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
                        log.d { "onGetChildren: downloads → ${artists.size} artists" }
                        resultList.addAll(artists.map { artist ->
                            MediaItem.Builder()
                                .setMediaId("dl_artist|$artist")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(artist)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_ARTIST)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("dl_artist|")) {
                        // Albums for one album-artist, drawn from downloads.
                        // Grouped/keyed by albumGroupId (names are not unique);
                        // the id payload is `<artist>|<groupId>` like `album|`.
                        val artist = parentId.substringAfter("dl_artist|")
                        val albums = repository.getDownloadedTracks()
                            .filter { autoAlbumArtist(it) == artist }
                            .groupBy { it.albumGroupId }
                            .entries
                            .sortedBy { (_, tracks) -> albumOf(tracks.first()).lowercase() }
                        log.d { "onGetChildren: dl_artist '$artist' → ${albums.size} albums" }
                        resultList.addAll(albums.map { (groupId, albumTracks) ->
                            val artKey = albumTracks.firstOrNull()?.fileKey ?: ""
                            MediaItem.Builder()
                                .setMediaId("dl_album|$artist|$groupId")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(albumOf(albumTracks.first()))
                                        .setSubtitle(artist)
                                        .setArtworkUri(artworkUriFor(artKey))
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                                        .setIsPlayable(true)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("dl_album|")) {
                        // Payload is `<artist>|<groupId>`; the groupId contains
                        // '|' itself, so split once at the first '|' only.
                        val payload = parentId.substringAfter("dl_album|")
                        val artist = payload.substringBefore("|")
                        val groupId = payload.substringAfter("|")
                        val tracks = downloadedAlbumTracks(artist, groupId)
                        if (tracks.isNotEmpty()) {
                            resultList.add(
                                MediaItem.Builder()
                                    .setMediaId("dl_play|$artist|$groupId")
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
                        log.d { "onGetChildren: artists → ${artists.size}" }
                        resultList.addAll(artists.map { artist ->
                            MediaItem.Builder()
                                .setMediaId("artist|$artist")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(artist)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_ARTIST)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("artist|")) {
                        val artistName = parentId.substring("artist|".length)
                        val albums = repository.getAlbumsByArtist(artistName)
                        log.d { "onGetChildren: artist '$artistName' → ${albums.size} albums" }
                        resultList.addAll(albums.map { album ->
                            MediaItem.Builder()
                                .setMediaId("album|${album.albumArtist}|${album.albumGroupId}")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(album.name)
                                        .setArtworkUri(artworkUriFor(album.artworkFileKey))
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("browse|")) {
                        // Mirror the library's MCWS Browse hierarchy. A node with
                        // child categories is a folder; one with only files is a
                        // leaf — opening a leaf starts playback of its tracks.
                        val nodeId = parentId.substringAfter("browse|")
                        val children = repository.getBrowseChildren(nodeId)
                        log.d { "onGetChildren: browse node=$nodeId → ${children.size} children" }
                        if (children.isNotEmpty()) {
                            resultList.addAll(children.map { item ->
                                MediaItem.Builder()
                                    .setMediaId("browse|${item.key}")
                                    .setMediaMetadata(
                                        MediaMetadata.Builder()
                                            .setTitle(item.name)
                                            .setArtworkUri(browseNodeArtUri(item.key))
                                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                                            .setIsPlayable(false)
                                            .setIsBrowsable(true)
                                            .build()
                                    )
                                    .build()
                            })
                        } else {
                            val tracks = repository.getBrowseFiles(nodeId)
                            log.d { "onGetChildren: browse leaf node=$nodeId → ${tracks.size} files" }
                            if (tracks.isNotEmpty()) {
                                // Only fire playback on the first page request,
                                // not on each pagination call for the same leaf.
                                if (page == 0) {
                                    log.i { "browse leaf opened (node=$nodeId): playing ${tracks.size} tracks" }
                                    val facade = this@PlaybackService.appContainer.facade
                                    withContext(Dispatchers.Main) {
                                        val hasConnection = !facade.currentServerHost.isNullOrEmpty()
                                        val targetZone = if (hasConnection) Zone.Local else Zone.Offline
                                        if (facade.activeZone.value.id != targetZone.id) {
                                            log.i { "browse leaf playback: switching zone ${facade.activeZone.value.id} → ${targetZone.id}" }
                                            facade.setZone(targetZone, skipLoadQueue = true)
                                        }
                                        facade.setQueue(tracks, 0)
                                    }
                                }
                                resultList.addAll(mapTracksToMediaItems(tracks))
                            }
                        }
                    } else if (parentId.startsWith("album|")) {
                        val payload = parentId.substringAfter("album|")
                        val album = resolveAlbumFromMediaId(payload)
                        log.d { "onGetChildren: album $payload → ${album?.name ?: "NOT FOUND"}" }
                        if (album != null) {
                            resultList.add(
                                MediaItem.Builder()
                                    .setMediaId("play_album|${album.albumArtist}|${album.albumGroupId}")
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

                    log.d {
                        "onGetChildren(parentId=$parentId) done: total=${resultList.size} " +
                            "paged=${pagedList.size} in ${System.currentTimeMillis() - startedAtMs}ms"
                    }
                    future.set(LibraryResult.ofItemList(ImmutableList.copyOf(pagedList), params))
                } catch (e: Exception) {
                    log.e(e) { "onGetChildren(parentId=$parentId) failed" }
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
            log.d { "onGetItem(mediaId=$mediaId, browser=${browser.packageName})" }
            val future = SettableFuture.create<LibraryResult<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val track = getTrackByKey(mediaId)
                    if (track != null) {
                        log.d { "onGetItem($mediaId) → '${track.name}'" }
                        future.set(LibraryResult.ofItem(mapTrackToMediaItem(track), null))
                    } else {
                        log.w { "onGetItem($mediaId) → not found (RESULT_ERROR_BAD_VALUE)" }
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                } catch (e: Exception) {
                    log.e(e) { "onGetItem(mediaId=$mediaId) failed" }
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
            log.i { "onSearch(query=$query, browser=${browser.packageName})" }
            val future = SettableFuture.create<LibraryResult<Void>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val db = this@PlaybackService.appContainer.database
                    val allTracks = db.downloadedTrackDao().getAllTracks()
                    val resolver = VoiceSearchResolver()
                    val searchResult = resolver.resolve(query, null, allTracks)
                    log.d {
                        "onSearch(query=$query): ${searchResult.tracks.size} matches " +
                            "from ${allTracks.size} downloaded tracks"
                    }

                    currentSearchResults[query] = mapTracksToMediaItems(searchResult.tracks.map { it.toTrack() })

                    session.notifySearchResultChanged(browser, query, searchResult.tracks.size, params)
                    future.set(LibraryResult.ofVoid())
                } catch (e: Exception) {
                    log.e(e) { "onSearch(query=$query) failed" }
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
            log.d { "onGetSearchResult(query=$query, page=$page) → ${pagedList.size} of ${list.size}" }
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
            log.d {
                "onSetMediaItems(items=${mediaItems.size}, firstId=${firstItem?.mediaId}, " +
                    "startIndex=$startIndex, query=$query, controller=${controller.packageName})"
            }

            val future = SettableFuture.create<MediaSession.MediaItemsWithStartPosition>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val repository = this@PlaybackService.appContainer.libraryRepository
                    val facade = this@PlaybackService.appContainer.facade

                    withContext(Dispatchers.Main) {
                        val hasConnection = !facade.currentServerHost.isNullOrEmpty()
                        val targetZone = if (hasConnection) Zone.Local else Zone.Offline
                        if (facade.activeZone.value.id != targetZone.id) {
                            log.i { "onSetMediaItems: switching zone ${facade.activeZone.value.id} → ${targetZone.id}" }
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
                        log.i {
                            "onSetMediaItems: voice search '$query' → ${matchedTracks.size} tracks " +
                                "(shuffle=${searchResult.forceShuffle})"
                        }

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
                            log.w { "onSetMediaItems: voice search '$query' matched nothing" }
                            future.set(MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L))
                        }
                        return@launch
                    }

                    val firstItem = mediaItems.firstOrNull()
                    val mediaId = firstItem?.mediaId ?: ""

                    val queueTracks = mutableListOf<Track>()
                    var resolvedStartIndex = 0

                    if (mediaId.startsWith("dl_album|") || mediaId.startsWith("dl_play|")) {
                        // Payload is `<artist>|<groupId>` (groupId contains '|')
                        // — split once at the first '|' only.
                        val payload = mediaId.substringAfter("|")
                        val artist = payload.substringBefore("|")
                        val groupId = payload.substringAfter("|")
                        log.d { "onSetMediaItems: downloaded album artist=$artist groupId=$groupId" }
                        queueTracks.addAll(downloadedAlbumTracks(artist, groupId))
                    } else if (mediaId.startsWith("album|") || mediaId.startsWith("play_album|")) {
                        val payload = mediaId.substringAfter("|")
                        try {
                            val album = resolveAlbumFromMediaId(payload)
                            val albumTracks = album?.let { repository.getAlbumTracks(it) }
                            log.d {
                                "onSetMediaItems: album $payload → " +
                                    "${if (album == null) "NOT FOUND" else "'${album.name}' ${albumTracks?.size ?: 0} tracks"}"
                            }
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
                        log.d { "onSetMediaItems: resolved ${resolvedTracks.size}/${mediaItems.size} items by key" }

                        if (resolvedTracks.isNotEmpty()) {
                            val selectedTrack = resolvedTracks.first()
                            try {
                                // Track and Album share the same albumGroupId
                                // definition, so the track pinpoints its own
                                // album exactly — no name matching. The album
                                // artist (auto fallback to artist) scopes the
                                // lookup to that artist's albums.
                                val album = repository.getAlbumByGroupId(
                                    selectedTrack.albumArtist.ifBlank { selectedTrack.artist },
                                    selectedTrack.albumGroupId,
                                )
                                val albumTracks = album?.let { repository.getAlbumTracks(it) }
                                if (!albumTracks.isNullOrEmpty()) {
                                    queueTracks.addAll(albumTracks)
                                    val idx = albumTracks.indexOfFirst { it.fileKey == selectedTrack.fileKey }
                                    if (idx >= 0) {
                                        resolvedStartIndex = idx
                                    }
                                    log.d {
                                        "onSetMediaItems: expanded '${selectedTrack.name}' to album " +
                                            "'${selectedTrack.album}' (${albumTracks.size} tracks, start=$resolvedStartIndex)"
                                    }
                                }
                            } catch (e: Exception) {
                                log.w(e) { "onSetMediaItems: track→album lookup failed" }
                            }

                            if (queueTracks.isEmpty()) {
                                log.d { "onSetMediaItems: no album context, queueing ${resolvedTracks.size} resolved tracks as-is" }
                                queueTracks.addAll(resolvedTracks)
                                resolvedStartIndex = 0
                            }
                        }
                    }

                    if (queueTracks.isNotEmpty()) {
                        log.i { "onSetMediaItems: playing ${queueTracks.size} tracks from start=$resolvedStartIndex" }
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
                                    log.d {
                                        "onSetMediaItems: persisted queue zone=${targetZone.id} " +
                                            "tracks=${queueTracks.size} index=$resolvedStartIndex"
                                    }
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
                        log.w { "onSetMediaItems: nothing resolved, passing ${mediaItems.size} items through" }
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
                    log.e(e) { "onSetMediaItems(firstId=${firstItem?.mediaId}) failed" }
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
            log.d { "onAddMediaItems(items=${mediaItems.size}, controller=${controller.packageName})" }
            val future = SettableFuture.create<List<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val resolvedItems = mediaItems.map { item ->
                        val track = getTrackByKey(item.mediaId)
                        if (track != null) mapTrackToMediaItem(track) else item
                    }
                    log.d { "onAddMediaItems: resolved ${resolvedItems.size} items" }
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
