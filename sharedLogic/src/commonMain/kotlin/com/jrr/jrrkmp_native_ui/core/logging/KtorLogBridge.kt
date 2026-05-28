package com.jrr.jrrkmp_native_ui.core.logging

import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

/**
 * Bridges Ktor's [Logger] interface into Kermit so HTTP request/response
 * traces land in the same stream as the rest of the app's logs.
 *
 * Used by `createMcwsHttpClient`'s `install(Logging) { logger = KtorLogBridge }`.
 *
 * **Token redaction:** MCWS appends an auth token as a `Token=…` query param
 * on every request rather than using a header, so Ktor's built-in
 * `sanitizeHeader` doesn't catch it. We strip query-string tokens here so
 * shared debug logs never leak a bearer.
 */
internal object KtorLogBridge : KtorLogger {

    private val log = KermitLogger.withTag("net:Ktor")

    private val TOKEN_REGEX = Regex("([?&](?:Token|token|access_token|auth_token)=)([^&\\s\"]+)")

    override fun log(message: String) {
        val sanitized = TOKEN_REGEX.replace(message) { match ->
            val full = match.groupValues[2]
            val redacted = if (full.length <= 4) "***" else "***${full.takeLast(4)}"
            "${match.groupValues[1]}$redacted"
        }
        log.d { sanitized }
    }
}
