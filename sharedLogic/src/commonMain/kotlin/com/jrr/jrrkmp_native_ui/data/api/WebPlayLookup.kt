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
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable

private val log = Logger.withTag("net:WebPlay")

@Serializable
data class WebPlayLookupResult(
    val ip: String,
    val port: Int?,
    val httpsPort: Int?,
    val localIpList: List<String>
)

/**
 * Resolves a 6-digit JRiver "Access Key" to a server IP/port via JRiver's
 * webplay lookup endpoint. Each failure mode maps to a distinct [McwsError];
 * `internal` because callers go through `ServerRepository` (Arrow stays out
 * of the SKIE surface).
 */
internal suspend fun webPlayLookup(
    httpClient: HttpClient,
    accessKey: String,
): Either<McwsError, WebPlayLookupResult> = either<McwsError, WebPlayLookupResult> {
    log.i { "webPlayLookup(key=${accessKey.redact()})" }
    // HTTPS: this is a public JRiver service, and iOS/macOS App Transport
    // Security blocks plaintext to non-local hosts. The endpoint serves the
    // same response over TLS.
    val url = "https://webplay.jriver.com/libraryserver/lookup?id=$accessKey"
    val (code, xml) = catch({
        val response: HttpResponse = httpClient.get(url)
        response.status.value to response.bodyAsText()
    }) { e -> raise(McwsError.Unreachable("webplay.jriver.com", e)) }
    ensure(code in 200..299) { McwsError.HttpError(code) }
    val parsed = catch({ parseMcwsWebPlayLookup(xml) }) { e -> raise(McwsError.ParseError(e)) }

    val ip = ensureNotNull(parsed["ip"]) {
        McwsError.ParseError(IllegalStateException("lookup response missing 'ip' field"))
    }
    val port = parsed["port"]?.toIntOrNull()
    val httpsPort = parsed["httpsport"]?.toIntOrNull()
    val localIpList = parsed["localiplist"]
        ?.split(",")
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?: emptyList()

    WebPlayLookupResult(
        ip = ip,
        port = port,
        httpsPort = httpsPort,
        localIpList = localIpList
    )
}
    .onRight { log.i { "webPlayLookup → ip=${it.ip} port=${it.port} httpsPort=${it.httpsPort} localIps=${it.localIpList.size}" } }
    .onLeft { err -> log.w { "webPlayLookup failed: ${err.logSummary()}" } }
