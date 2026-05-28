package com.jrr.jrrkmp_native_ui.core.logging

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

/**
 * Redact a token-shaped string for safe logging. Returns a fixed marker for
 * null/empty values and the last 4 chars otherwise (matching the pattern used
 * for auth tokens, API keys, and similar bearer credentials).
 *
 * ```
 * log.d { "auth set token=${token.redact()}" }
 * ```
 */
fun String?.redact(): String =
    when {
        isNullOrEmpty() -> "<empty>"
        length <= 4 -> "***"
        else -> "***${takeLast(4)}"
    }

/**
 * Wrap a block in try/catch with structured logging at both success and
 * failure. The success branch logs at Debug; the failure branch logs at
 * Error with the throwable attached, then re-wraps as a [Result.failure].
 *
 * Use when you want logging without forcing the caller to write the
 * boilerplate.
 *
 * ```
 * val tracks = log.runCatchingLogged("loadTracks") {
 *     libraryRepository.getAlbumTracks(album)
 * }.getOrDefault(emptyList())
 * ```
 */
inline fun <T> Logger.runCatchingLogged(op: String, block: () -> T): Result<T> {
    return runCatching(block)
        .onSuccess { d { "$op ok" } }
        .onFailure {
            if (it is CancellationException) throw it
            e(it) { "$op failed" }
        }
}

/**
 * Add structured logging to a Flow's lifecycle. Logs start, every emission at
 * Verbose (via [summarise], so the caller controls volume), and completion
 * (Debug on normal completion, Error on failure). Cancellation is unlogged —
 * it's normal coroutine teardown noise.
 *
 * ```
 * combine(a, b, c) { ... }
 *     .logged(log, "albumDetailState") { "content=${it.contentState::class.simpleName}" }
 *     .launchIn(viewModelScope)
 * ```
 */
fun <T> Flow<T>.logged(
    log: Logger,
    name: String,
    summarise: (T) -> String = { it.toString() },
): Flow<T> =
    onStart { log.d { "$name flow start" } }
        .onEach { log.v { "$name → ${summarise(it)}" } }
        .catch { e ->
            if (e !is CancellationException) log.e(e) { "$name flow failed" }
            throw e
        }
        .onCompletion { e ->
            when (e) {
                null -> log.d { "$name flow complete" }
                is CancellationException -> Unit
                else -> Unit // already logged in .catch
            }
        }
