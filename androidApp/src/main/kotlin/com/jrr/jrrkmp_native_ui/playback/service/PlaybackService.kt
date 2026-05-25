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
import com.jrr.jrrkmp_native_ui.JrrDependencies
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class PlaybackService : MediaLibraryService() {

    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val currentSearchResults = ConcurrentHashMap<String, List<MediaItem>>()

    override fun onCreate() {
        super.onCreate()
        val facade = JrrDependencies.getAudioPlayerFacade(this)
        val player = JrrDependencies.getLocalPlayerHandler(this).getUnderlyingPlayer()

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
            val facade = JrrDependencies.getAudioPlayerFacade(this)
            facade.setZone(Zone.AndroidAuto, skipLoadQueue = true)
        }
        return mediaLibrarySession
    }

    override fun onDestroy() {
        serviceScope.cancel()
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
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
                    val db = JrrDependencies.getDatabase(this@PlaybackService)
                    val allTracks = db.downloadedTrackDao().getAllTracks()
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
                                .setMediaId("recently_played")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Recently Played")
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_TITLES)
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
                                .setMediaId("albums")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle("Albums")
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        )
                    } else if (parentId == "downloads") {
                        resultList.addAll(allTracks.sortedBy { it.name }.map { mapTrackToMediaItem(it) })
                    } else if (parentId == "recently_played") {
                        val recent = emptyList<com.jrr.jrrkmp_native_ui.data.db.entity.DownloadedTrackEntity>()
                        resultList.addAll(recent.map { mapTrackToMediaItem(it) })
                    } else if (parentId == "artists") {
                        val artists = allTracks.map { it.artist }.distinct().sorted()
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
                        val albums = allTracks.filter { it.artist.equals(artistName, ignoreCase = true) }
                            .map { it.album }.distinct().sorted()
                        resultList.addAll(albums.map { album ->
                            MediaItem.Builder()
                                .setMediaId("artist_album|$artistName|$album")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(album)
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("artist_album|")) {
                        val parts = parentId.split("|")
                        val artistName = parts.getOrNull(1) ?: ""
                        val albumName = parts.getOrNull(2) ?: ""
                        val tracks = allTracks.filter {
                            it.artist.equals(artistName, ignoreCase = true) && it.album.equals(albumName, ignoreCase = true)
                        }.sortedWith(compareBy({ it.trackNumber ?: 0 }, { it.name }))
                        resultList.addAll(tracks.map { mapTrackToMediaItem(it) })
                    } else if (parentId == "albums") {
                        val albums = allTracks.map { it.album }.distinct().sorted()
                        resultList.addAll(albums.map { album ->
                            MediaItem.Builder()
                                .setMediaId("album|$album")
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(album)
                                        .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                                        .setIsPlayable(false)
                                        .setIsBrowsable(true)
                                        .build()
                                )
                                .build()
                        })
                    } else if (parentId.startsWith("album|")) {
                        val albumName = parentId.substring("album|".length)
                        val tracks = allTracks.filter { it.album.equals(albumName, ignoreCase = true) }
                            .sortedWith(compareBy({ it.trackNumber ?: 0 }, { it.name }))
                        resultList.addAll(tracks.map { mapTrackToMediaItem(it) })
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
                    val db = JrrDependencies.getDatabase(this@PlaybackService)
                    val track = db.downloadedTrackDao().getTrack(mediaId)
                    if (track != null) {
                        future.set(LibraryResult.ofItem(mapTrackToMediaItem(track), null))
                    } else {
                        future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                    }
                } catch (e: Exception) {
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
                    val db = JrrDependencies.getDatabase(this@PlaybackService)
                    val allTracks = db.downloadedTrackDao().getAllTracks()
                    val resolver = VoiceSearchResolver()
                    val searchResult = resolver.resolve(query, null, allTracks)

                    currentSearchResults[query] = searchResult.tracks.map { mapTrackToMediaItem(it) }

                    session.notifySearchResultChanged(browser, query, searchResult.tracks.size, params)
                    future.set(LibraryResult.ofVoid())
                } catch (e: Exception) {
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

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>
        ): ListenableFuture<List<MediaItem>> {
            val firstItem = mediaItems.firstOrNull()
            val query = firstItem?.requestMetadata?.searchQuery
            val extras = firstItem?.requestMetadata?.extras

            if (query != null || extras != null) {
                val future = SettableFuture.create<List<MediaItem>>()
                serviceScope.launch(Dispatchers.IO) {
                    try {
                        val db = JrrDependencies.getDatabase(this@PlaybackService)
                        val allTracks = db.downloadedTrackDao().getAllTracks()

                        val resolver = VoiceSearchResolver()
                        val searchResult = resolver.resolve(query, extras, allTracks)
                        val matchedTracks = searchResult.tracks

                        if (matchedTracks.isNotEmpty()) {
                            val items = matchedTracks.map { mapTrackToMediaItem(it) }
                            withContext(Dispatchers.Main) {
                                val facade = JrrDependencies.getAudioPlayerFacade(this@PlaybackService)
                                facade.setZone(Zone.AndroidAuto)
                                val trackInfos = matchedTracks.map { it.toTrack() }
                                facade.setQueue(trackInfos, 0)
                                if (searchResult.forceShuffle) {
                                    facade.setShuffleMode(com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode.ON)
                                } else {
                                    facade.setShuffleMode(com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode.OFF)
                                }
                                facade.play()
                            }
                            future.set(items)
                        } else {
                            future.set(emptyList())
                        }
                    } catch (e: Exception) {
                        future.set(mediaItems)
                    }
                }
                return future
            }

            // Normal item play
            val future = SettableFuture.create<List<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                try {
                    val db = JrrDependencies.getDatabase(this@PlaybackService)
                    val itemsToPlay = mediaItems.mapNotNull { item ->
                        val track = db.downloadedTrackDao().getTrack(item.mediaId)
                        track?.let { mapTrackToMediaItem(it) }
                    }
                    if (itemsToPlay.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            val facade = JrrDependencies.getAudioPlayerFacade(this@PlaybackService)
                            facade.setZone(Zone.AndroidAuto)
                            val trackInfos = itemsToPlay.mapNotNull { item ->
                                val track = db.downloadedTrackDao().getTrack(item.mediaId)
                                track?.toTrack()
                            }
                            facade.setQueue(trackInfos, 0)
                            facade.play()
                        }
                        future.set(itemsToPlay)
                    } else {
                        future.set(mediaItems)
                    }
                } catch (e: Exception) {
                    future.set(mediaItems)
                }
            }
            return future
        }
    }

    private fun mapTrackToMediaItem(track: com.jrr.jrrkmp_native_ui.data.db.entity.DownloadedTrackEntity): MediaItem {
        val artUri = Uri.parse("content://com.jrr.jrrkmp_native_ui.fileprovider/downloads/art_${track.fileKey}.jpg")
        val fileUri = Uri.fromFile(File(track.filePath))
        return MediaItem.Builder()
            .setMediaId(track.fileKey)
            .setUri(fileUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(artUri)
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
            )
            .build()
    }
}
