package com.jrr.jrrkmp_native_ui.data.api

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.redact
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
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
 * webplay lookup endpoint.
 */
suspend fun webPlayLookup(httpClient: HttpClient, accessKey: String): WebPlayLookupResult? {
    log.i { "webPlayLookup(key=${accessKey.redact()})" }
    val url = "http://webplay.jriver.com/libraryserver/lookup?id=$accessKey"
    val xml = try {
        val response: HttpResponse = httpClient.get(url)
        if (response.status.value in 200..299) {
            response.bodyAsText()
        } else {
            log.w { "webPlayLookup: HTTP ${response.status.value}" }
            return null
        }
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        log.e(e) { "webPlayLookup HTTP failed" }
        return null
    }

    return try {
        val parsed = parseMcwsWebPlayLookup(xml)

        val ip = parsed["ip"] ?: run {
            log.w { "webPlayLookup: response missing 'ip' field" }
            return null
        }
        val port = parsed["port"]?.toIntOrNull()
        val httpsPort = parsed["httpsport"]?.toIntOrNull()
        val localIpList = parsed["localiplist"]
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()

        log.i { "webPlayLookup → ip=$ip port=$port httpsPort=$httpsPort localIps=${localIpList.size}" }
        WebPlayLookupResult(
            ip = ip,
            port = port,
            httpsPort = httpsPort,
            localIpList = localIpList
        )
    } catch (e: Exception) {
        log.e(e) { "webPlayLookup: failed to parse response" }
        null
    }
}
