package com.jrr.jrrkmp_native_ui.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments

object McwsClient {
    var currentHost: String? = null
    var currentPort: Int = 52199
    var currentUseSsl: Boolean = false
    var currentSslPort: Int = 52200
    var currentToken: String? = null

    private val jsonConfiguration = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    val httpClient = createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(jsonConfiguration)
        }
    }

    fun getBaseUrl(): String? {
        val host = currentHost ?: return null
        val scheme = if (currentUseSsl) "https" else "http"
        val port = if (currentUseSsl) currentSslPort else currentPort
        return "$scheme://$host:$port/MCWS/v1"
    }

    fun buildImageUrl(fileKey: String): String {
        val base = getBaseUrl() ?: return ""
        val token = currentToken ?: return ""
        return "$base/File/GetImage?File=$fileKey&Type=Thumbnail&Width=300&Height=300&Square=1&Token=$token"
    }

    suspend fun getRaw(url: String): String? {
        return try {
            val response: HttpResponse = httpClient.get(url)
            if (response.status.value in 200..299) {
                response.bodyAsText()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getMcwsJson(endpoint: String, params: Map<String, String> = emptyMap()): String? {
        val base = getBaseUrl() ?: return null
        val token = currentToken ?: return null
        
        val url = URLBuilder(base).apply {
            val cleanEndpoint = if (endpoint.startsWith("/")) endpoint.substring(1) else endpoint
            appendPathSegments(cleanEndpoint)
            parameters.append("Action", "JSON")
            parameters.append("Token", token)
            params.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }.buildString()

        return getRaw(url)
    }

    suspend fun getMcwsXml(endpoint: String, params: Map<String, String> = emptyMap()): String? {
        val base = getBaseUrl() ?: return null
        val token = currentToken
        
        val url = URLBuilder(base).apply {
            val cleanEndpoint = if (endpoint.startsWith("/")) endpoint.substring(1) else endpoint
            appendPathSegments(cleanEndpoint)
            if (token != null) {
                parameters.append("Token", token)
            }
            params.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }.buildString()

        return getRaw(url)
    }
}
