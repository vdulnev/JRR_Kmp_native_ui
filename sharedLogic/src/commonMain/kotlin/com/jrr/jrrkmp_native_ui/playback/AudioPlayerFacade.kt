package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueStateEntity
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueTrackEntity
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.PlayerStatus
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AudioPlayerFacade(
    private val database: JrrDatabase?,
    private val localPlayerEngine: LocalPlayerEngine,
    private val saveLastActiveZoneId: (String) -> Unit = {},
    private val loadLastActiveZoneId: () -> String? = { null },
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val coroutineScope = CoroutineScope(mainDispatcher + SupervisorJob())

    val currentServerHost: String?
        get() = McwsClient.currentHost

    val currentServerPort: Int
        get() = McwsClient.currentPort

    val currentServerUseSsl: Boolean
        get() = McwsClient.currentUseSsl

    val currentServerSslPort: Int
        get() = McwsClient.currentSslPort

    // Remote Playback Handler
    private val remoteHandler = McwsRemotePlayerHandler()

    // State Flows
    private val _activeZone = MutableStateFlow<Zone>(Zone.Offline)
    val activeZone: StateFlow<Zone> = _activeZone

    private val _playerStatus = MutableStateFlow<PlayerStatus?>(null)
    val playerStatus: StateFlow<PlayerStatus?> = _playerStatus

    val localQueue: StateFlow<List<Track>> = localPlayerEngine.queue

    private var isPollingEnabled = true
    private var pollingJob: Job? = null
    private var localProgressJob: Job? = null

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    init {
        // Collect local state and merge with facade status
        coroutineScope.launch {
            combine(
                localPlayerEngine.queue,
                localPlayerEngine.playbackState,
                localPlayerEngine.currentIndex,
                localPlayerEngine.volume,
                combine(localPlayerEngine.shuffleMode, localPlayerEngine.repeatMode) { s, r -> Pair(s, r) }
            ) { queue, state, index, vol, shuffleRepeat ->
                val zone = _activeZone.value
                val track = if (queue.isNotEmpty()) queue[index] else null
                if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
                    val position = localPlayerEngine.getCurrentPosition()
                    val duration = localPlayerEngine.getDuration()
                    _playerStatus.value = PlayerStatus(
                        zoneId = zone.id,
                        zoneName = zone.name,
                        state = state,
                        positionMs = position,
                        durationMs = duration,
                        volume = vol,
                        isMuted = vol == 0.0f,
                        shuffleMode = shuffleRepeat.first,
                        repeatMode = shuffleRepeat.second,
                        playingNowPosition = index,
                        playingNowTracks = localPlayerEngine.getQueueSize(),
                        trackAlbum = track?.album ?: "",
                        trackArtist = track?.artist ?: "",
                        trackName = track?.name ?: "",
                        sampleRate = track?.sampleRate ?: -1
                    )
                }
            }.collect()
        }

        // Start local progress tick
        startLocalProgressPolling()

        // Track index changes to update queue state in the database
        coroutineScope.launch {
            localPlayerEngine.currentIndex.collect { index ->
                val zone = _activeZone.value
                if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
                    withContext(ioDispatcher) {
                        try {
                            database?.localQueueStateDao()?.insertOrUpdate(
                                LocalQueueStateEntity(
                                    zoneId = zone.id,
                                    currentIndex = index
                                )
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        // Restore last active zone ID
        val lastZoneId = loadLastActiveZoneId()
        if (lastZoneId != null) {
            // Setup default active zone if known
            val defaultZone = when (lastZoneId) {
                Zone.Local.id -> Zone.Local
                Zone.Offline.id -> Zone.Offline
                Zone.AndroidAuto.id -> Zone.AndroidAuto
                else -> Zone(id = lastZoneId, name = "Remote Zone", guid = "")
            }
            setZone(defaultZone)
        } else {
            setZone(Zone.Offline)
        }
    }

    fun setServerConnection(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        authToken: String?
    ) {
        McwsClient.currentHost = host
        McwsClient.currentPort = port
        McwsClient.currentUseSsl = useSsl
        McwsClient.currentSslPort = sslPort
        McwsClient.currentToken = authToken
    }

    fun setZone(zone: Zone, skipLoadQueue: Boolean = false) {
        val oldZone = _activeZone.value
        if (oldZone.isLocal || oldZone.isOffline || oldZone.isAndroidAuto) {
            saveQueueState(oldZone.id)
            if (!zone.isLocal && !zone.isOffline && !zone.isAndroidAuto) {
                localPlayerEngine.stop()
            }
        } else {
            if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
                coroutineScope.launch(ioDispatcher) {
                    remoteHandler.stop(oldZone.id)
                }
            }
        }

        _activeZone.value = zone
        _playerStatus.value = null

        saveLastActiveZoneId(zone.id)

        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            if (skipLoadQueue) {
                localPlayerEngine.clearQueue()
            } else {
                loadQueueState(zone.id, skipPlayback = true)
            }
        }

        startPolling()
    }

    fun setPollingEnabled(enabled: Boolean) {
        isPollingEnabled = enabled
        if (enabled) {
            startPolling()
        } else {
            pollingJob?.cancel()
            pollingJob = null
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            return
        }
        pollingJob = coroutineScope.launch(ioDispatcher) {
            while (isActive) {
                if (isPollingEnabled) {
                    val status = remoteHandler.getPlaybackInfo(zone.id)
                    if (status != null) {
                        _playerStatus.value = status
                    }
                }
                delay(1000L)
            }
        }
    }

    private fun startLocalProgressPolling() {
        localProgressJob?.cancel()
        localProgressJob = coroutineScope.launch {
            while (isActive) {
                val zone = _activeZone.value
                if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
                    val current = _playerStatus.value
                    if (current != null && current.state == PlaybackState.PLAYING) {
                        _playerStatus.value = current.copy(
                            positionMs = localPlayerEngine.getCurrentPosition(),
                            durationMs = localPlayerEngine.getDuration()
                        )
                    }
                }
                delay(500L)
            }
        }
    }

    fun setQueue(tracks: List<Track>, startIndex: Int) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.setQueue(tracks, startIndex)
            localPlayerEngine.play()
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                val keys = tracks.joinToString(",") { it.fileKey }
                remoteHandler.seekTo(zone.id, 0L)
                val success = McwsClient.executeCommand("Playback/PlayByKey", mapOf(
                    "Key" to keys,
                    "Zone" to zone.id,
                    "ZoneType" to "ID",
                    "Location" to "0"
                ))
                if (success) {
                    if (startIndex > 0) {
                        McwsClient.executeCommand("Playback/PlayByIndex", mapOf(
                            "Index" to startIndex.toString(),
                            "Zone" to zone.id,
                            "ZoneType" to "ID"
                        ))
                    } else {
                        remoteHandler.play(zone.id)
                    }
                }
            }
        }
    }

    fun play() {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.play()
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.play(zone.id)
            }
        }
    }

    fun pause() {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.pause()
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.pause(zone.id)
            }
        }
    }

    fun stop() {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.stop()
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.stop(zone.id)
            }
        }
    }

    fun next() {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.playNext()
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.next(zone.id)
            }
        }
    }

    fun previous() {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.playPrevious()
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.previous(zone.id)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.seekTo(positionMs)
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.seekTo(zone.id, positionMs)
            }
        }
    }

    fun setVolume(level: Float) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.setVolume(level)
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.setVolume(zone.id, level)
            }
        }
    }

    fun setShuffleMode(mode: ShuffleMode) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.setShuffleMode(mode)
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.setShuffleMode(zone.id, mode)
            }
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.setRepeatMode(mode)
        } else {
            coroutineScope.launch(ioDispatcher) {
                remoteHandler.setRepeatMode(zone.id, mode)
            }
        }
    }

    fun playByIndex(index: Int) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.playByIndex(index)
        } else {
            coroutineScope.launch(ioDispatcher) {
                McwsClient.executeCommand("Playback/PlayByIndex", mapOf(
                    "Index" to index.toString(),
                    "Zone" to zone.id,
                    "ZoneType" to "ID"
                ))
            }
        }
    }

    fun removeQueueTrack(index: Int) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.removeTrack(index)
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                McwsClient.executeCommand("Playback/EditPlaylist", mapOf(
                    "Action" to "Remove",
                    "Source" to index.toString(),
                    "Zone" to zone.id
                ))
            }
        }
    }

    fun moveQueueTrack(from: Int, to: Int) {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.moveTrack(from, to)
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                McwsClient.executeCommand("Playback/EditPlaylist", mapOf(
                    "Action" to "Move",
                    "Source" to from.toString(),
                    "Target" to to.toString(),
                    "Zone" to zone.id
                ))
            }
        }
    }

    fun clearQueue() {
        val zone = _activeZone.value
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.clearQueue()
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                McwsClient.executeCommand("Playback/ClearPlaylist", mapOf(
                    "Zone" to zone.id,
                    "ZoneType" to "ID"
                ))
            }
        }
    }

    fun saveQueueState(zoneId: String) {
        val db = database ?: return
        coroutineScope.launch(ioDispatcher) {
            try {
                val queueTracks = localPlayerEngine.getQueue()
                val currentIndex = localPlayerEngine.currentIndex.value

                db.localQueueTrackDao().clearQueueForZone(zoneId)
                val entities = queueTracks.mapIndexed { idx, track ->
                    LocalQueueTrackEntity(
                        zoneId = zoneId,
                        fileKey = track.fileKey,
                        trackJson = json.encodeToString(track),
                        position = idx
                    )
                }
                db.localQueueTrackDao().insertAll(entities)

                db.localQueueStateDao().insertOrUpdate(
                    LocalQueueStateEntity(
                        zoneId = zoneId,
                        currentIndex = currentIndex
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadQueueState(zoneId: String, skipPlayback: Boolean = false) {
        val db = database ?: return
        coroutineScope.launch(ioDispatcher) {
            try {
                val dbTracks = db.localQueueTrackDao().getTracksForZone(zoneId)
                val dbState = db.localQueueStateDao().getStateForZone(zoneId)

                val tracks = dbTracks.mapNotNull {
                    try {
                        json.decodeFromString<Track>(it.trackJson)
                    } catch (e: Exception) {
                        null
                    }
                }

                val currentIndex = dbState?.currentIndex ?: -1

                withContext(Dispatchers.Main) {
                    if (tracks.isNotEmpty()) {
                        localPlayerEngine.setQueue(tracks, currentIndex)
                        if (!skipPlayback && currentIndex >= 0) {
                            localPlayerEngine.play()
                        }
                    } else {
                        localPlayerEngine.clearQueue()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
