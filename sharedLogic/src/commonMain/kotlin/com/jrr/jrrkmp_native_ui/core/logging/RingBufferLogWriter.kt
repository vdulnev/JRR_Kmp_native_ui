package com.jrr.jrrkmp_native_ui.core.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * In-memory ring of the last [CAPACITY] formatted log lines. Backs the
 * "Share debug log" Settings feature so users can attach recent activity to a
 * bug report without needing to attach to a debugger.
 *
 * Thread-safe via [synchronized] on a single lock — the volume of log calls
 * is far below what would justify a more sophisticated structure.
 */
internal object RingBufferLogWriter : LogWriter() {

    private const val CAPACITY = 1000

    private val lock = SynchronizedObject()
    private val ring = ArrayDeque<String>(CAPACITY)

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val line = buildString {
            append(severity.name.take(1)) // V/D/I/W/E/A
            append(' ')
            append('[')
            append(tag)
            append("] ")
            append(message)
            if (throwable != null) {
                append('\n')
                append(throwable.stackTraceToString())
            }
        }
        synchronized(lock) {
            if (ring.size == CAPACITY) ring.removeFirst()
            ring.addLast(line)
        }
    }

    fun snapshot(): List<String> = synchronized(lock) { ring.toList() }

    /** Test/debug hook — clears the ring. Not exposed via [AppLogger]. */
    internal fun clear() = synchronized(lock) { ring.clear() }
}
