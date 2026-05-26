package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.api.McwsXmlParser
import com.jrr.jrrkmp_native_ui.data.api.WebPlayLookup
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class McwsServerData(
    val host: String,
    val port: Int,
    val useSsl: Boolean,
    val sslPort: Int,
    val token: String?
)

class ServerRepository(
    private val database: JrrDatabase,
    private val httpClient: HttpClient,
) {
    private val serverDao = database.savedServerDao()

    private val _activeServer = MutableStateFlow<McwsServerData?>(null)
    val activeServer: StateFlow<McwsServerData?> = _activeServer.asStateFlow()

    fun setActiveServer(host: String, port: Int, useSsl: Boolean, sslPort: Int, token: String?) {
        _activeServer.value = if (host.isEmpty()) null else McwsServerData(host, port, useSsl, sslPort, token)
    }

    suspend fun lookupAccessKey(key: String): WebPlayLookup.LookupResult? = withContext(Dispatchers.IO) {
        WebPlayLookup.lookup(httpClient, key)
    }

    suspend fun authenticate(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        username: String,
        passwordVal: String
    ): String? = withContext(Dispatchers.IO) {
        val scheme = if (useSsl) "https" else "http"
        val actualPort = if (useSsl) sslPort else port
        val url = "$scheme://$host:$actualPort/MCWS/v1/Authenticate"
        
        try {
            val authValue = "$username:$passwordVal"
            val credential = "Basic ${authValue.encodeBase64()}"
            val response: HttpResponse = httpClient.get(url) {
                header("Authorization", credential)
                header("No-Auth", "true")
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val xmlResponse = McwsXmlParser.parseResponse(body)
                if (xmlResponse.status == "OK") {
                    xmlResponse.items["Token"]
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun checkAlive(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        token: String
    ): String? = withContext(Dispatchers.IO) {
        val scheme = if (useSsl) "https" else "http"
        val actualPort = if (useSsl) sslPort else port
        val url = "$scheme://$host:$actualPort/MCWS/v1/Alive?Token=$token"
        
        try {
            val response: HttpResponse = httpClient.get(url) {
                header("No-Auth", "true")
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val xmlResponse = McwsXmlParser.parseResponse(body)
                if (xmlResponse.status == "OK") {
                    xmlResponse.items["FriendlyName"] ?: "JRiver Server"
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllServers(): List<SavedServerEntity> = withContext(Dispatchers.IO) {
        serverDao.getAllServers()
    }

    suspend fun getLastUsedServer(): SavedServerEntity? = withContext(Dispatchers.IO) {
        serverDao.getLastUsedServer()
    }

    suspend fun saveServer(server: SavedServerEntity) = withContext(Dispatchers.IO) {
        serverDao.insert(server)
    }

    suspend fun deleteServer(server: SavedServerEntity) = withContext(Dispatchers.IO) {
        serverDao.delete(server)
    }
}
