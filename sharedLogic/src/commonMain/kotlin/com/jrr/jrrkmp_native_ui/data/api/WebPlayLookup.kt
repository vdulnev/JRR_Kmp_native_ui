package com.jrr.jrrkmp_native_ui.data.api

import kotlinx.serialization.Serializable

object WebPlayLookup {

    @Serializable
    data class LookupResult(
        val ip: String,
        val port: Int?,
        val httpsPort: Int?,
        val localIpList: List<String>
    )

    suspend fun lookup(accessKey: String): LookupResult? {
        val url = "http://webplay.jriver.com/libraryserver/lookup?id=$accessKey"
        val xml = McwsClient.getRaw(url) ?: return null
        
        return try {
            val parsed = McwsXmlParser.parseWebPlayLookup(xml)

            val ip = parsed["ip"] ?: return null
            val port = parsed["port"]?.toIntOrNull()
            val httpsPort = parsed["httpsport"]?.toIntOrNull()
            val localIpList = parsed["localiplist"]
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?: emptyList()

            LookupResult(
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
}
