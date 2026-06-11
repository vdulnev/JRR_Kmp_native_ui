package com.jrr.jrrkmp_native_ui.playback

import arrow.resilience.Schedule
import arrow.resilience.retry
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.redact
import com.jrr.jrrkmp_native_ui.core.logging.runCatchingLogged
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueStateEntity
import com.jrr.jrrkmp_native_ui.data.db.entity.LocalQueueTrackEntity
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.domain.model.LocalAudioQuality
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.PlayerStatus
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val log = Logger.withTag("playback:Facade")

/**
 * One remote-status poll came back empty: transport failure or MCWS rejection
 * (detail already logged by McwsClient). Drives the poll backoff [Schedule].
 */
private class PollUnavailableException(zoneId: String) :
    Exception("playback info unavailable zone=$zoneId")

class AudioPlayerFacade(
    private val database: JrrDatabase?,
    private val localPlayerEngine: LocalPlayerEngine,
    private val mcwsClient: McwsClient,
    private val serverRepository: ServerRepository? = null,
    private val saveLastActiveZoneId: (String) -> Unit = {},
    private val loadLastActiveZoneId: () -> String? = { null },
    private val saveLocalAudioQuality: (String) -> Unit = {},
    private val loadLocalAudioQuality: () -> String? = { null },
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    /**
     * Six-arg secondary constructor for Swift. SKIE 0.10.x's
     * DefaultArgumentInterop only generates Swift overloads for top-level
     * functions, not class constructors, so Swift would otherwise only see
     * the 8-arg init. This ctor gives iOS a clean entry point that uses the
     * dispatcher defaults without exposing `Dispatchers` to the Swift side.
     */
    constructor(
        database: JrrDatabase?,
        localPlayerEngine: LocalPlayerEngine,
        mcwsClient: McwsClient,
        serverRepository: ServerRepository?,
        saveLastActiveZoneId: (String) -> Unit,
        loadLastActiveZoneId: () -> String?,
        saveLocalAudioQuality: (String) -> Unit,
        loadLocalAudioQuality: () -> String?,
    ) : this(
        database = database,
        localPlayerEngine = localPlayerEngine,
        mcwsClient = mcwsClient,
        serverRepository = serverRepository,
        saveLastActiveZoneId = saveLastActiveZoneId,
        loadLastActiveZoneId = loadLastActiveZoneId,
        saveLocalAudioQuality = saveLocalAudioQuality,
        loadLocalAudioQuality = loadLocalAudioQuality,
        mainDispatcher = Dispatchers.Main,
        ioDispatcher = Dispatchers.IO,
    )

    private val coroutineScope = CoroutineScope(mainDispatcher + SupervisorJob())

    /**
     * Platform hook fired after a disconnect has been handled centrally —
     * Android cancels the WorkManager download jobs here, iOS cancels its
     * URLSession download tasks. The shared cleanup (zone switch, polling
     * stop, download-job rows) has already run when this is invoked.
     */
    var onDownloadsCancelled: (() -> Unit)? = null

    val currentServerHost: String?
        get() = serverRepository?.activeServer?.value?.host

    val currentServerPort: Int
        get() = serverRepository?.activeServer?.value?.port ?: 52199

    val currentServerUseSsl: Boolean
        get() = serverRepository?.activeServer?.value?.useSsl ?: false

    val currentServerSslPort: Int
        get() = serverRepository?.activeServer?.value?.sslPort ?: 52200

    val currentServerToken: String?
        get() = serverRepository?.activeServer?.value?.token

    /**
     * Identity of the connected real server (empty when none). Favorites are
     * scoped to it; ViewModels observe this to swap favorites on connect.
     */
    val activeServerId: StateFlow<String> =
        serverRepository?.activeServerId ?: MutableStateFlow("")

    // Local audio quality (server-side transcode level for streaming/downloads)
    private val _localAudioQuality = MutableStateFlow(
        LocalAudioQuality.fromName(loadLocalAudioQuality()),
    )
    val localAudioQuality: StateFlow<LocalAudioQuality> = _localAudioQuality

    /** Sync getter for native players that build MCWS URLs (iOS CorePlayer). */
    val currentLocalAudioQuality: LocalAudioQuality
        get() = _localAudioQuality.value

    /**
     * Single source of truth for the MCWS `GetFile` stream/download URL, shared
     * by every platform's track request (Android [LocalPlayerHandler],
     * iOS/macOS/tvOS CorePlayer + DownloadManager). Built from the **active
     * server only** — returns an empty string when none is connected. `Channels=2`
     * forces a stereo downmix; [playback] adds `Playback=1` for streaming
     * (downloads pass false). The transcode level comes from [currentLocalAudioQuality].
     */
    fun streamUrl(fileKey: String, playback: Boolean): String {
        val host = currentServerHost
        if (host.isNullOrEmpty()) return ""
        val useSsl = currentServerUseSsl
        val scheme = if (useSsl) "https" else "http"
        val port = if (useSsl) currentServerSslPort else currentServerPort
        val token = currentServerToken ?: ""
        val playbackParam = if (playback) "&Playback=1" else ""
        return "$scheme://$host:$port/MCWS/v1/File/GetFile?File=$fileKey" +
            "&FileType=Key$playbackParam&${currentLocalAudioQuality.mcwsParams}&Channels=2&Token=$token"
    }

    // Artwork URLs. Like [streamUrl] these are the single source of truth for
    // playback-layer consumers (Android player/Auto service, iOS players,
    // download pipelines) and are built from the active server only — empty
    // string when none. They delegate to [McwsClient], which shares the same
    // active-server flow, so UI callers using the client directly stay in sync.

    /** 300×300 square thumbnail for a file key (Now Playing, lists). */
    fun artworkUrl(fileKey: String): String =
        if (fileKey.isEmpty()) "" else mcwsClient.buildImageUrl(fileKey)

    /** Full-size cover for a file key — persisted next to downloaded tracks. */
    fun fullArtworkUrl(fileKey: String): String =
        if (fileKey.isEmpty()) "" else mcwsClient.buildFullImageUrl(fileKey)

    /** Representative cover for a Browse-tree node (album/category folder). */
    fun browseNodeArtUrl(nodeId: String): String = mcwsClient.buildBrowseImageUrl(nodeId)

    // Remote Playback Handler
    private val remoteHandler = McwsRemotePlayerHandler(mcwsClient)

    // State Flows
    private val _activeZone = MutableStateFlow<Zone>(Zone.Offline)
    val activeZone: StateFlow<Zone> = _activeZone

    // `_activeZone` starts at Offline as a placeholder. The first real [setZone]
    // (the facade restoring the persisted zone in `init`) must NOT save the
    // placeholder zone's queue — the engine is empty at that point, so it would
    // persist an empty queue over the saved one and wipe it. Flips true after
    // the first setZone.
    private var hasSetInitialZone = false

    private val _playerStatus = MutableStateFlow<PlayerStatus?>(null)
    val playerStatus: StateFlow<PlayerStatus?> = _playerStatus

    private val _connectionToken = MutableStateFlow<String?>(serverRepository?.activeServer?.value?.token)
    val connectionToken: StateFlow<String?> = _connectionToken

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
        log.d { "init" }
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
                val track = if (index in queue.indices) queue[index] else null
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
                        sampleRate = track?.sampleRate ?: -1,
                        trackFileKey = track?.fileKey ?: ""
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
                            log.e(e) { "persist currentIndex failed zone=${zone.id} index=$index" }
                        }
                    }
                }
            }
        }

        // Restore last active zone ID
        val lastZoneId = loadLastActiveZoneId()
        if (lastZoneId != null) {
            log.i { "restoring last active zone: $lastZoneId" }
            // Setup default active zone if known
            val defaultZone = when (lastZoneId) {
                Zone.Local.id -> Zone.Local
                Zone.Offline.id -> Zone.Offline
                Zone.AndroidAuto.id -> Zone.AndroidAuto
                else -> Zone(id = lastZoneId, name = "Remote Zone", guid = "")
            }
            setZone(defaultZone)
        } else {
            log.i { "no last active zone, defaulting to Offline" }
            setZone(Zone.Offline)
        }

        // Asynchronously restore last active server connection if it is not
        // already set. Retried with jittered exponential backoff so one
        // flaky-WiFi moment at app start doesn't leave the app disconnected
        // (recoverActiveServer itself gives up after a single round-trip).
        if (serverRepository != null && currentServerHost.isNullOrEmpty()) {
            coroutineScope.launch(ioDispatcher) {
                val recoverySchedule = Schedule.exponential<Throwable>(500.milliseconds)
                    .jittered()
                    .and(Schedule.recurs(4))
                    .log { _, _ -> log.i { "server recovery attempt failed; retrying" } }
                val recovered = try {
                    recoverySchedule.retry {
                        val ok = serverRepository.recoverActiveServer { host, port, useSsl, sslPort, token ->
                            withContext(mainDispatcher) {
                                setServerConnection(host, port, useSsl, sslPort, token)
                            }
                        }
                        check(ok) { "recovery attempt failed" }
                    }
                    true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    false
                }
                if (!recovered) log.w { "server recovery gave up after retries" }
            }
        }

        // Central disconnect enforcement: every path that clears the active
        // server (phone Disconnect, TV Disconnect, anything future) flows
        // through serverRepository.activeServer, so reacting here guarantees
        // ALL online activity stops on disconnect regardless of which UI
        // triggered it. Only the connected → disconnected transition fires;
        // the initial not-yet-recovered state does not.
        if (serverRepository != null) {
            coroutineScope.launch {
                var wasConnected = false
                serverRepository.activeServer.collect { server ->
                    val connected = !server?.host.isNullOrEmpty()
                    if (wasConnected && !connected) {
                        handleServerDisconnected()
                    }
                    wasConnected = connected
                }
            }
        }
    }

    /**
     * Stop all online activity after the active server was cleared:
     * - switch to the Offline zone (stops server streaming and remote-zone
     *   control, loads the downloaded-only queue; local playback continues)
     * - polling stops as part of the zone switch
     * - delete unfinished download jobs, then let the platform cancel its
     *   download transport (WorkManager / URLSession) via [onDownloadsCancelled]
     */
    private fun handleServerDisconnected() {
        log.i { "active server cleared → stopping online activity" }
        if (!_activeZone.value.isOffline) {
            setZone(Zone.Offline)
        } else {
            // Already offline-zoned: still make sure remote polling is dead.
            pollingJob?.cancel()
            pollingJob = null
        }
        coroutineScope.launch(ioDispatcher) {
            try {
                val removed = database?.downloadJobDao()?.deleteUnfinishedJobs() ?: 0
                log.i { "disconnect: removed $removed unfinished download jobs" }
            } catch (e: Exception) {
                log.e(e) { "disconnect: failed to clear download jobs" }
            }
            onDownloadsCancelled?.invoke()
        }
    }

    fun setServerConnection(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        authToken: String?
    ) {
        log.i { "setServerConnection(host=$host port=$port ssl=$useSsl sslPort=$sslPort token=${authToken.redact()})" }
        serverRepository?.setActiveServer(host, port, useSsl, sslPort, authToken)
        _connectionToken.value = authToken

        // Establishing a real server connection (non-empty host) means we're no
        // longer offline. If the active zone is Offline — e.g. the app started
        // offline and the user just logged in from the Server screen — switch to
        // the Local zone so the UI leaves offline mode (library/zones come
        // online). A disconnect calls this with an empty host and sets Offline
        // explicitly, so it must NOT trigger this. `setZone` already rebuilds the
        // local queue with the now-known host, so we return early.
        if (host.isNotEmpty() && _activeZone.value.isOffline) {
            log.i { "server connection established while Offline → switching to Local zone" }
            setZone(Zone.Local)
            return
        }

        // At startup the saved local queue is restored *before* auto-connect
        // resolves the active server, so any streaming items get built with an
        // empty host and fail with a connection error. Now that the server is
        // known, rebuild the local queue so those items pick up the real host —
        // but only while idle, so we never interrupt active playback.
        val zone = _activeZone.value
        val state = localPlayerEngine.playbackState.value
        if ((zone.isLocal || zone.isOffline || zone.isAndroidAuto) &&
            localPlayerEngine.getQueueSize() > 0 &&
            state != PlaybackState.PLAYING
        ) {
            log.i { "server connected while idle local queue present — rebuilding queue URLs" }
            loadQueueState(zone.id, skipPlayback = true)
        }
    }

    fun setLocalAudioQuality(quality: LocalAudioQuality) {
        log.i { "setLocalAudioQuality(${quality.name})" }
        _localAudioQuality.value = quality
        saveLocalAudioQuality(quality.name)
    }

    fun setZone(zone: Zone, skipLoadQueue: Boolean = false) {
        val oldZone = _activeZone.value
        val isInitialZoneSet = !hasSetInitialZone
        hasSetInitialZone = true
        // Only tear down / persist the previous zone when we're genuinely
        // switching AWAY from it: not on the initial restore (engine still
        // empty — saving would wipe the persisted queue), and not when
        // re-selecting the already-active zone.
        val leavingZone = !isInitialZoneSet && oldZone.id != zone.id
        log.i {
            "setZone(${zone.id}, skipLoadQueue=$skipLoadQueue) from=${oldZone.id} " +
                "initial=$isInitialZoneSet leaving=$leavingZone"
        }
        if (leavingZone) {
            if (oldZone.isLocal || oldZone.isOffline || oldZone.isAndroidAuto) {
                saveQueueState(oldZone.id)
                if (!zone.isLocal && !zone.isOffline && !zone.isAndroidAuto) {
                    log.d { "stopping local engine before remote zone switch" }
                    localPlayerEngine.stop()
                }
            } else {
                if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
                    log.d { "stopping remote zone ${oldZone.id} before local switch" }
                    coroutineScope.launch(ioDispatcher) {
                        remoteHandler.stop(oldZone.id)
                    }
                }
            }
        }

        _activeZone.value = zone
        _playerStatus.value = null

        if (!zone.isTransientZone) {
            saveLastActiveZoneId(zone.id)
        }



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
        log.d { "setPollingEnabled($enabled)" }
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
            log.v { "startPolling skipped (zone=${zone.id} is local-class)" }
            return
        }
        log.d { "startPolling zone=${zone.id}" }
        // While the server answers, poll at a fixed 1s cadence. While it
        // fails, back off exponentially (1s → 2s → 4s …) and settle on a 30s
        // probe instead of hammering a down server every second. The first
        // successful poll resets the cadence to 1s.
        val backoff = Schedule.exponential<Throwable>(1.seconds)
            .doWhile { _, duration -> duration < 30.seconds }
            .andThen(Schedule.spaced(30.seconds))
            .log { error, _ -> log.w { "poll failed zone=${zone.id} (${error.message}); backing off" } }
        pollingJob = coroutineScope.launch(ioDispatcher) {
            while (isActive) {
                backoff.retry { pollOnce(zone) }
                delay(1000L)
            }
        }
    }

    /** One poll tick; throws [PollUnavailableException] so [startPolling]'s
     *  backoff schedule can react (the client returns null on all failures). */
    private suspend fun pollOnce(zone: Zone) {
        if (!isPollingEnabled) return
        val status = remoteHandler.getPlaybackInfo(zone.id)
            ?: throw PollUnavailableException(zone.id)
        log.v { "poll → state=${status.state} pos=${status.positionMs}ms" }
        _playerStatus.value = status
    }

    private fun startLocalProgressPolling() {
        localProgressJob?.cancel()
        log.v { "startLocalProgressPolling" }
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
        log.d { "setQueue(${tracks.size} tracks, startIndex=$startIndex) zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.setQueue(tracks, startIndex)
            localPlayerEngine.play()
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                try {
                    // Clear the remote queue first to ensure a clean queue
                    mcwsClient.executeCommand("Playback/ClearPlaylist", mapOf(
                        "Zone" to zone.id,
                        "ZoneType" to "ID"
                    ))
                    val keys = tracks.joinToString(",") { it.fileKey }
                    remoteHandler.seekTo(zone.id, 0L)
                    val success = mcwsClient.executeCommand("Playback/PlayByKey", mapOf(
                        "Key" to keys,
                        "Zone" to zone.id,
                        "ZoneType" to "ID",
                        "Location" to "0"
                    ))
                    if (success) {
                        if (startIndex > 0) {
                            mcwsClient.executeCommand("Playback/PlayByIndex", mapOf(
                                "Index" to startIndex.toString(),
                                "Zone" to zone.id,
                                "ZoneType" to "ID"
                            ))
                        } else {
                            remoteHandler.play(zone.id)
                        }
                    } else {
                        log.w { "remote setQueue: PlayByKey returned false zone=${zone.id}" }
                    }
                } catch (e: Exception) {
                    log.e(e) { "remote setQueue failed zone=${zone.id}" }
                }
            }
        }
    }

    fun addTracks(tracks: List<Track>) {
        val zone = _activeZone.value
        log.d { "addTracks(${tracks.size}) zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.addTracks(tracks)
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                try {
                    val keys = tracks.joinToString(",") { it.fileKey }
                    mcwsClient.executeCommand("Playback/PlayByKey", mapOf(
                        "Key" to keys,
                        "Zone" to zone.id,
                        "ZoneType" to "ID",
                        "Location" to "END"
                    ))
                } catch (e: Exception) {
                    log.e(e) { "remote addTracks failed zone=${zone.id}" }
                }
            }
        }
    }

    fun playNextTracks(tracks: List<Track>) {
        val zone = _activeZone.value
        log.d { "playNextTracks(${tracks.size}) zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.insertTracksNext(tracks)
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                try {
                    val keys = tracks.joinToString(",") { it.fileKey }
                    mcwsClient.executeCommand("Playback/PlayByKey", mapOf(
                        "Key" to keys,
                        "Zone" to zone.id,
                        "ZoneType" to "ID",
                        "Location" to "NEXT"
                    ))
                } catch (e: Exception) {
                    log.e(e) { "remote playNextTracks failed zone=${zone.id}" }
                }
            }
        }
    }

    fun play() {
        val zone = _activeZone.value
        log.d { "play() zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.play()
        } else {
            coroutineScope.launch(ioDispatcher) {
                if (_playerStatus.value?.state == PlaybackState.PAUSED) {
                    remoteHandler.resume(zone.id)
                } else {
                    remoteHandler.play(zone.id)
                }
            }
        }
    }

    fun pause() {
        val zone = _activeZone.value
        log.d { "pause() zone=${zone.id}" }
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
        log.d { "stop() zone=${zone.id}" }
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
        log.d { "next() zone=${zone.id}" }
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
        log.d { "previous() zone=${zone.id}" }
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
        log.d { "seekTo(${positionMs}ms) zone=${zone.id}" }
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
        log.d { "setVolume($level) zone=${zone.id}" }
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
        log.d { "setShuffleMode($mode) zone=${zone.id}" }
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
        log.d { "setRepeatMode($mode) zone=${zone.id}" }
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
        log.d { "playByIndex($index) zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.playByIndex(index)
        } else {
            coroutineScope.launch(ioDispatcher) {
                mcwsClient.executeCommand("Playback/PlayByIndex", mapOf(
                    "Index" to index.toString(),
                    "Zone" to zone.id,
                    "ZoneType" to "ID"
                ))
            }
        }
    }

    fun removeQueueTrack(index: Int) {
        val zone = _activeZone.value
        log.d { "removeQueueTrack($index) zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.removeTrack(index)
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                mcwsClient.executeCommand("Playback/EditPlaylist", mapOf(
                    "Action" to "Remove",
                    "Source" to index.toString(),
                    "Zone" to zone.id
                ))
            }
        }
    }

    fun moveQueueTrack(from: Int, to: Int) {
        val zone = _activeZone.value
        log.d { "moveQueueTrack($from → $to) zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.moveTrack(from, to)
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                mcwsClient.executeCommand("Playback/EditPlaylist", mapOf(
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
        log.d { "clearQueue() zone=${zone.id}" }
        if (zone.isLocal || zone.isOffline || zone.isAndroidAuto) {
            localPlayerEngine.clearQueue()
            saveQueueState(zone.id)
        } else {
            coroutineScope.launch(ioDispatcher) {
                mcwsClient.executeCommand("Playback/ClearPlaylist", mapOf(
                    "Zone" to zone.id,
                    "ZoneType" to "ID"
                ))
            }
        }
    }

    fun saveQueueState(zoneId: String) {
        val db = database ?: return
        coroutineScope.launch(ioDispatcher) {
            log.runCatchingLogged("saveQueueState(zone=$zoneId)") {
                val queueTracks = localPlayerEngine.getQueue()
                val currentIndex = localPlayerEngine.currentIndex.value
                log.v { "saveQueueState zone=$zoneId tracks=${queueTracks.size} currentIndex=$currentIndex" }

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
                        log.w(e) { "loadQueueState: failed to decode track, skipping" }
                        null
                    }
                }

                val currentIndex = dbState?.currentIndex ?: -1
                log.d { "loadQueueState zone=$zoneId restored=${tracks.size} currentIndex=$currentIndex" }

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
                log.e(e) { "loadQueueState failed zone=$zoneId" }
            }
        }
    }
}
