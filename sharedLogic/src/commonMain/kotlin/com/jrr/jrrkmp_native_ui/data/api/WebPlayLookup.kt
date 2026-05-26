package com.jrr.jrrkmp_native_ui.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable

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
    val url = "http://webplay.jriver.com/libraryserver/lookup?id=$accessKey"
    val xml = try {
        val response: HttpResponse = httpClient.get(url)
        if (response.status.value in 200..299) response.bodyAsText() else return null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }

    return try {
        val parsed = parseMcwsWebPlayLookup(xml)

        val ip = parsed["ip"] ?: return null
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
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
