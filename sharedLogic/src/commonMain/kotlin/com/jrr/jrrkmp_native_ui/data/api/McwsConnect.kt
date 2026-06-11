package com.jrr.jrrkmp_native_ui.data.api

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.redact
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.util.encodeBase64

private val log = Logger.withTag("net:McwsConnect")

/*
 * Typed MCWS connection-flow calls: each failure mode maps to a distinct
 * [McwsError] instead of collapsing into `null`. Arrow's `raise.catch`
 * rethrows fatal throwables — including CancellationException — so normal
 * coroutine teardown passes through untouched.
 *
 * `internal`: callers go through [com.jrr.jrrkmp_native_ui.data.repository.ServerRepository],
 * whose Either-returning members are @HiddenFromObjC (Arrow never crosses SKIE).
 */

private fun mcwsUrl(host: String, port: Int, useSsl: Boolean, sslPort: Int, endpoint: String): String {
    val scheme = if (useSsl) "https" else "http"
    val actualPort = if (useSsl) sslPort else port
    return "$scheme://$host:$actualPort/MCWS/v1/$endpoint"
}

/** `Authenticate` with HTTP basic credentials; Right = session token. */
internal suspend fun mcwsAuthenticate(
    httpClient: HttpClient,
    host: String,
    port: Int,
    useSsl: Boolean,
    sslPort: Int,
    username: String,
    passwordVal: String,
): Either<McwsError, String> = either<McwsError, String> {
    log.i { "authenticate(host=$host port=${if (useSsl) sslPort else port} ssl=$useSsl user=$username)" }
    val credential = "Basic ${"$username:$passwordVal".encodeBase64()}"
    val (code, body) = catch({
        val response: HttpResponse = httpClient.get(mcwsUrl(host, port, useSsl, sslPort, "Authenticate")) {
            header("Authorization", credential)
            header("No-Auth", "true")
        }
        response.status.value to response.bodyAsText()
    }) { e -> raise(McwsError.Unreachable(host, e)) }
    ensure(code in 200..299) { McwsError.HttpError(code) }
    // parseMcwsResponse is lenient (never throws): garbage XML yields
    // status="Failure" and lands in McwsRejected below.
    val xml = parseMcwsResponse(body)
    ensure(xml.status == "OK") { McwsError.McwsRejected(xml.status) }
    ensureNotNull(xml.items["Token"]) {
        McwsError.ParseError(IllegalStateException("Authenticate response missing Token"))
    }
}
    .onRight { token -> log.i { "authenticate ok host=$host token=${token.redact()}" } }
    .onLeft { err -> log.w { "authenticate failed host=$host: ${err.logSummary()}" } }

/** `Alive` with an existing token; Right = the server's FriendlyName. */
internal suspend fun mcwsCheckAlive(
    httpClient: HttpClient,
    host: String,
    port: Int,
    useSsl: Boolean,
    sslPort: Int,
    token: String,
): Either<McwsError, String> = either<McwsError, String> {
    log.d { "checkAlive(host=$host port=${if (useSsl) sslPort else port} ssl=$useSsl)" }
    val (code, body) = catch({
        val response: HttpResponse = httpClient.get(mcwsUrl(host, port, useSsl, sslPort, "Alive?Token=$token")) {
            header("No-Auth", "true")
        }
        response.status.value to response.bodyAsText()
    }) { e -> raise(McwsError.Unreachable(host, e)) }
    ensure(code in 200..299) { McwsError.HttpError(code) }
    val xml = parseMcwsResponse(body)
    ensure(xml.status == "OK") { McwsError.McwsRejected(xml.status) }
    xml.items["FriendlyName"] ?: "JRiver Server"
}
    .onRight { name -> log.d { "checkAlive ok host=$host friendlyName=$name" } }
    .onLeft { err -> log.w { "checkAlive failed host=$host: ${err.logSummary()}" } }
