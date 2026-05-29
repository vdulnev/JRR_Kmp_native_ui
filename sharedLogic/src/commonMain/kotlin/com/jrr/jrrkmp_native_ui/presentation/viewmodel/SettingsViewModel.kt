package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.jrr.jrrkmp_native_ui.core.logging.AppLogger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadJobEntity
import com.jrr.jrrkmp_native_ui.domain.model.LocalAudioQuality
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val log = Logger.withTag("vm:Settings")

private fun SettingsViewState.summary(): String = buildString {
    append("offline=$isOfflineMode")
    if (serverHost != null) {
        append(" server=$serverHost:${if (useSsl) serverSslPort else serverPort}")
        if (useSsl) append("(ssl)")
    }
    append(" downloaded=$downloadedTracksCount")
    append(" jobs=${downloadJobs.size}")
    append(" quality=${localAudioQuality.name}")
    append(" sev=$logSeverity")
    if (transientError != null) append(" err=$transientError")
}

data class SettingsViewState(
    val isOfflineMode: Boolean = true,
    val serverHost: String? = null,
    val useSsl: Boolean = false,
    // Match AudioPlayerFacade.currentServerPort / currentServerSslPort
    // defaults — visible only for the one frame before the first combine
    // emission.
    val serverPort: Int = 52199,
    val serverSslPort: Int = 52200,
    val downloadedTracksCount: Int = 0,
    val downloadJobs: List<DownloadJobEntity> = emptyList(),
    /** Whether the running build is a dev/debug variant. Gates the log-severity
     *  selector UI — release users don't need to see it. */
    val isDebugBuild: Boolean = false,
    /** Current minimum-severity floor enforced by Kermit. Surfaced for the
     *  Settings → Logging picker so dev users can flip levels at runtime. */
    val logSeverity: Severity = Severity.Info,
    /** Server-side transcode quality applied to streaming + downloads. */
    val localAudioQuality: LocalAudioQuality = LocalAudioQuality.LOSSLESS,
    val transientError: String? = null,
)

class SettingsViewModel(
    private val facade: AudioPlayerFacade,
    private val database: JrrDatabase,
    private val clearPhysicalDownloads: () -> Unit,
    isDebugBuild: Boolean = false,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsViewState(
            isDebugBuild = isDebugBuild,
            logSeverity = if (isDebugBuild) Severity.Verbose else Severity.Info,
        ),
    )
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()

    init {
        log.d { "init isDebugBuild=$isDebugBuild" }
        combine(
            facade.activeZone,
            // connectionToken is a trigger only — its value is unused, but its
            // change rebuilds the state so the facade's currentServer* getters
            // are re-read when the active server swaps.
            facade.connectionToken,
            database.downloadedTrackDao().getAllTracksFlow(),
            database.downloadJobDao().getAllJobsFlow(),
        ) { activeZone, _, downloadedTracks, jobs ->
            val isOffline = activeZone.isOffline || facade.currentServerHost.isNullOrEmpty()
            // Build only the fields that depend on upstream flows; preserve the
            // user-controlled isDebugBuild + logSeverity from the existing
            // _state so the picker doesn't reset on every emission.
            isOffline to OnlineFields(
                serverHost = facade.currentServerHost,
                useSsl = facade.currentServerUseSsl,
                serverPort = facade.currentServerPort,
                serverSslPort = facade.currentServerSslPort,
                downloadedTracksCount = downloadedTracks.size,
                downloadJobs = jobs,
            )
        }
            .distinctUntilChanged()
            .onEach { (isOffline, f) ->
                _state.update {
                    it.copy(
                        isOfflineMode = isOffline,
                        serverHost = f.serverHost,
                        useSsl = f.useSsl,
                        serverPort = f.serverPort,
                        serverSslPort = f.serverSslPort,
                        downloadedTracksCount = f.downloadedTracksCount,
                        downloadJobs = f.downloadJobs,
                    )
                }
            }
            .launchIn(viewModelScope)

        facade.localAudioQuality
            .onEach { quality -> _state.update { it.copy(localAudioQuality = quality) } }
            .launchIn(viewModelScope)

        state.logged(log, "state") { it.summary() }.launchIn(viewModelScope)
    }

    /** Tuple holding the upstream-derived fields. Lets the combine block stay
     *  focused on what *depends* on upstream flows while the user-controlled
     *  preferences live separately. */
    private data class OnlineFields(
        val serverHost: String?,
        val useSsl: Boolean,
        val serverPort: Int,
        val serverSslPort: Int,
        val downloadedTracksCount: Int,
        val downloadJobs: List<DownloadJobEntity>,
    )

    fun clearDownloads() {
        log.i { "clearDownloads()" }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    clearPhysicalDownloads()
                    val before = database.downloadedTrackDao().getAllTracks().size
                    database.downloadedTrackDao().deleteAll()
                    log.i { "cleared $before downloaded tracks + filesystem" }
                } catch (e: Exception) {
                    log.e(e) { "clearDownloads failed" }
                    _state.update {
                        it.copy(
                            transientError = "Failed to clear downloads: ${e.message ?: "unknown error"}",
                        )
                    }
                }
            }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }

    /**
     * Apply a new server-side transcode quality for local-zone streaming and
     * downloads. Persisted by the facade via its platform-provided lambda, so
     * the choice survives restarts. Already-downloaded files keep whatever
     * format they were fetched in.
     */
    fun setLocalAudioQuality(quality: LocalAudioQuality) {
        log.i { "setLocalAudioQuality(${quality.name})" }
        facade.setLocalAudioQuality(quality)
    }

    /**
     * Apply a new minimum-severity floor to Kermit. Affects all subsequent
     * log calls across the app immediately. Persisted only in the running
     * process — restart resets to the build-default floor.
     */
    fun setLogSeverity(severity: Severity) {
        log.i { "setLogSeverity($severity)" }
        AppLogger.setMinSeverity(severity)
        _state.update { it.copy(logSeverity = severity) }
    }

    /**
     * Returns a snapshot of the last 1000 log lines (newest last) for the
     * "Share debug log" Settings action. Each platform's Settings screen
     * funnels this through the OS share sheet.
     */
    fun exportLogText(): String {
        log.i { "exportLogText()" }
        return AppLogger.recentLogs().joinToString("\n")
    }
}
