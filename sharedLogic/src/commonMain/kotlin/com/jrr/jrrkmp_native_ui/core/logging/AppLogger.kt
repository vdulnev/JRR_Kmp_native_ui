package com.jrr.jrrkmp_native_ui.core.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.platformLogWriter

/**
 * Application-wide logging bootstrap. Call [configure] exactly once at process
 * start from each platform's entry point (Android `Application.onCreate`,
 * iOS `AppDelegate.didFinishLaunchingWithOptions`).
 *
 * After [configure] runs, log via the standard Kermit API:
 *
 * ```
 * private val log = Logger.withTag("vm:Library")
 * log.d { "init" }
 * log.e(throwable) { "loadTracks failed" }
 * ```
 *
 * Severity convention (see CLAUDE.md):
 * - **Verbose** — high-volume internals (poll ticks, per-frame state). Dev only.
 * - **Debug** — lifecycle, state transitions, user actions, network calls. Default in dev.
 * - **Info** — events you'd want in a bug report (connect, zone switch). Default in release.
 * - **Warn** — recoverable problems (fallback, retry).
 * - **Error** — caught exceptions, failed ops. Always with [Throwable].
 * - **Assert** — programmer-error contract violations.
 */
object AppLogger {

    /**
     * Wires Kermit's writer list and minimum severity. Idempotent — safe to
     * call again, e.g. from the in-app severity selector in dev builds.
     *
     * @param isDebug picks the default min severity. Verbose in dev, Info in
     *   release.
     * @param extraWriters callers (typically the platform Application) may
     *   append platform-specific writers, e.g. Crashlytics. The platform
     *   default ([platformLogWriter] → Logcat / OSLog) and the in-memory
     *   [RingBufferLogWriter] are always installed.
     */
    fun configure(isDebug: Boolean, extraWriters: List<LogWriter> = emptyList()) {
        val severity = if (isDebug) Severity.Verbose else Severity.Info
        Logger.setMinSeverity(severity)
        Logger.setLogWriters(
            buildList {
                add(platformLogWriter())
                add(RingBufferLogWriter)
                addAll(extraWriters)
            },
        )
        Logger.setTag("JRR")
        Logger.i(tag = "AppLogger") {
            "configured isDebug=$isDebug severity=$severity writers=${extraWriters.size + 2}"
        }
    }

    /**
     * Dev-only: live minimum-severity adjustment. Wired from a Settings UI
     * picker so users sharing a bug report can flip to Verbose without a
     * rebuild.
     */
    fun setMinSeverity(severity: Severity) {
        Logger.setMinSeverity(severity)
        Logger.i(tag = "AppLogger") { "min severity → $severity" }
    }

    /**
     * Snapshot of the in-memory ring buffer for the "Share debug log" Settings
     * action. Returns the most recent log lines (up to [RingBufferLogWriter]'s
     * configured capacity), newest last.
     */
    fun recentLogs(): List<String> = RingBufferLogWriter.snapshot()
}
