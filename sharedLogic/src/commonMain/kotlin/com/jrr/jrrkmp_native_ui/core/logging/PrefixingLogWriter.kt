package com.jrr.jrrkmp_native_ui.core.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity

/**
 * Adapter that prepends [prefix] to the tag of every log message before
 * handing it to [delegate]. Applied uniformly to every writer wired in
 * `AppLogger.configure` so every log line — Logcat, OSLog, ring buffer,
 * future Crashlytics writer, etc. — sees a consistent `jrr:<…>` namespace
 * and the app's output stays trivially filterable amongst all other
 * processes' Logcat / Console output.
 *
 * Per-call-site `Logger.withTag("vm:Library")` stays untouched — the
 * prefix is applied transparently at write time.
 */
internal class PrefixingLogWriter(
    private val delegate: LogWriter,
    private val prefix: String,
) : LogWriter() {

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        delegate.log(severity, message, "$prefix$tag", throwable)
    }

    override fun isLoggable(tag: String, severity: Severity): Boolean {
        // Pass the prefixed tag through in case a future delegate wants to do
        // per-tag severity filtering (e.g. a writer that drops anything below
        // Warn for the `jrr:net:Ktor` namespace specifically).
        return delegate.isLoggable("$prefix$tag", severity)
    }
}
