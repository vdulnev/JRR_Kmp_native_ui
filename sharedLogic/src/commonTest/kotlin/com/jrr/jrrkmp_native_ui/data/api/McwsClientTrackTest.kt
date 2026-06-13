package com.jrr.jrrkmp_native_ui.data.api

import com.jrr.jrrkmp_native_ui.data.repository.McwsServerData
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Covers [McwsClient.getTrackByKey] — the single-track re-fetch that powers the
 * live "played" overlay. It must hit `Files/Search` with the `[Key]=` query and
 * surface the authoritative `Number Plays`.
 */
class McwsClientTrackTest {

    private val server = McwsServerData(
        host = "mc.local",
        port = 52199,
        useSsl = false,
        sslPort = 52200,
        token = "T",
    )

    private fun client(body: String): Pair<McwsClient, MutableList<String>> {
        val requestedUrls = mutableListOf<String>()
        val http = HttpClient(
            MockEngine { request ->
                requestedUrls.add(request.url.toString())
                respond(content = body, status = HttpStatusCode.OK)
            },
        )
        val mcws = McwsClient(httpClient = http, activeServerFlow = MutableStateFlow(server))
        return mcws to requestedUrls
    }

    @Test
    fun getTrackByKeyParsesNumberPlaysAndQueriesByKey() = runTest {
        val (mcws, urls) = client("""[{"Key":"key1","Name":"Track 1","Number Plays":"3"}]""")

        val track = mcws.getTrackByKey("key1")

        assertEquals(3, track?.numberPlays)
        assertEquals("Track 1", track?.name)
        val url = urls.single()
        assertTrue(url.contains("Files/Search"), "expected Files/Search endpoint, was $url")
        assertTrue(url.contains("Key"), "expected a [Key]= query, was $url")
    }

    @Test
    fun getTrackByKeyShortCircuitsOnEmptyKey() = runTest {
        val (mcws, urls) = client("[]")

        assertNull(mcws.getTrackByKey(""))
        assertTrue(urls.isEmpty(), "empty key must not hit the network")
    }

    @Test
    fun getTrackByKeyReturnsNullWhenNoMatch() = runTest {
        val (mcws, _) = client("[]")

        assertNull(mcws.getTrackByKey("missing"))
    }
}
