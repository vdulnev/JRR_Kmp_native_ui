package com.jrr.jrrkmp_native_ui.core.logging

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import co.touchlab.kermit.Logger

/**
 * Wraps an [SQLiteDriver] so every connection it opens forwards `prepare(sql)`
 * calls through to Kermit at Verbose under the `db:SQL` tag.
 *
 * Lifecycle calls (`open`, `close`) log at Debug. Per-row step/bind calls are
 * intentionally not wrapped — those run hundreds of times per query and Room
 * is faithful about which SQL it prepared.
 *
 * Use in `createDatabase` as `.setDriver(LoggingSQLiteDriver(BundledSQLiteDriver()))`.
 * Below the Verbose threshold (release builds default to Info), this adds
 * essentially zero overhead — the lambda message construction is skipped
 * and the call is a single delegated invocation.
 */
internal class LoggingSQLiteDriver(private val delegate: SQLiteDriver) : SQLiteDriver {

    private val log = Logger.withTag("db:SQL")

    override fun open(fileName: String): SQLiteConnection {
        log.d { "open($fileName)" }
        return LoggingSQLiteConnection(delegate.open(fileName), log)
    }
}

private class LoggingSQLiteConnection(
    private val delegate: SQLiteConnection,
    private val log: Logger,
) : SQLiteConnection {

    override fun prepare(sql: String): SQLiteStatement {
        // Single-line normalization keeps multi-line CREATE/SELECT statements
        // readable in Logcat / OSLog without losing information.
        log.v { sql.singleLine() }
        return delegate.prepare(sql)
    }

    override fun close() {
        log.d { "close" }
        delegate.close()
    }

    private fun String.singleLine(): String =
        replace(Regex("\\s+"), " ").trim()
}
