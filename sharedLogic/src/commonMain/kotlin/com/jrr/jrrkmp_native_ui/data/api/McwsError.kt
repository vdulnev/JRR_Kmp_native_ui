package com.jrr.jrrkmp_native_ui.data.api

/**
 * Typed failure of an MCWS connection-flow call (authenticate, alive check,
 * access-key lookup). Replaces the old `null` collapse where "wrong password"
 * and "server unreachable" were indistinguishable.
 *
 * Plain sealed interface (no Arrow types) so it crosses the SKIE boundary
 * cleanly; the `Either<McwsError, T>` carriers stay Kotlin-only.
 */
sealed interface McwsError {
    /** Transport-level failure: DNS, refused connection, TLS, timeout. */
    data class Unreachable(val host: String, val cause: Throwable) : McwsError

    /** Server answered with a non-2xx HTTP status. 401/403 = bad credentials. */
    data class HttpError(val code: Int) : McwsError

    /** MCWS answered `<Response Status="…">` with something other than OK. */
    data class McwsRejected(val status: String) : McwsError

    /** Response body wasn't the shape we expect (missing fields, bad XML). */
    data class ParseError(val cause: Throwable) : McwsError
}

/** Short, user-presentable description — toasts and status lines. */
fun McwsError.toUserMessage(): String = when (this) {
    is McwsError.Unreachable -> "Server unreachable — check the address and your network"
    is McwsError.HttpError -> when (code) {
        401, 403 -> "Wrong username or password"
        else -> "Server error (HTTP $code)"
    }
    is McwsError.McwsRejected -> "Server rejected the request (status $status)"
    is McwsError.ParseError -> "Unexpected response from server"
}

/** One-line summary for log statements (never includes tokens). */
fun McwsError.logSummary(): String = when (this) {
    is McwsError.Unreachable -> "unreachable host=$host cause=${cause::class.simpleName}: ${cause.message}"
    is McwsError.HttpError -> "http $code"
    is McwsError.McwsRejected -> "mcws status=$status"
    is McwsError.ParseError -> "parse: ${cause.message}"
}
