package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.api.McwsXmlParser
import com.jrr.jrrkmp_native_ui.data.api.WebPlayLookup
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.encodeBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ServerRepository(private val database: JrrDatabase) {
    private val serverDao = database.savedServerDao()

    suspend fun lookupAccessKey(key: String): WebPlayLookup.LookupResult? = withContext(Dispatchers.IO) {
        WebPlayLookup.lookup(key)
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
            val response: HttpResponse = McwsClient.httpClient.get(url) {
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
            val response: HttpResponse = McwsClient.httpClient.get(url) {
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
