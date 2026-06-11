package com.jrr.jrrkmp_native_ui.data.api

import arrow.core.Either
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Asserts WHICH [McwsError] each failure mode yields — the entire point of
 * the typed connection flow is that "wrong password" and "server unreachable"
 * are no longer the same `null`.
 */
class McwsConnectTest {

    private fun clientRespondingWith(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        HttpClient(MockEngine { respond(content = body, status = status) })

    private fun unreachableClient(message: String) =
        HttpClient(MockEngine { throw RuntimeException(message) })

    private suspend fun authenticate(client: HttpClient): Either<McwsError, String> =
        mcwsAuthenticate(client, host = "mc.local", port = 52199, useSsl = false, sslPort = 52200, username = "u", passwordVal = "p")

    private suspend fun checkAlive(client: HttpClient): Either<McwsError, String> =
        mcwsCheckAlive(client, host = "mc.local", port = 52199, useSsl = false, sslPort = 52200, token = "T")

    // --- authenticate ---

    @Test
    fun authenticateReturnsTokenOnOk() = runTest {
        val client = clientRespondingWith(
            """<Response Status="OK"><Item Name="Token">ABC123</Item></Response>"""
        )
        assertEquals(Either.Right("ABC123"), authenticate(client))
    }

    @Test
    fun authenticateMapsMcwsFailureToMcwsRejected() = runTest {
        val client = clientRespondingWith("""<Response Status="Failure"/>""")
        val err = authenticate(client).leftOrFail()
        assertEquals(McwsError.McwsRejected("Failure"), err)
    }

    @Test
    fun authenticateMapsHttp401ToHttpError() = runTest {
        val client = clientRespondingWith("nope", HttpStatusCode.Unauthorized)
        val err = authenticate(client).leftOrFail()
        assertEquals(McwsError.HttpError(401), err)
    }

    @Test
    fun authenticateMapsTransportFailureToUnreachable() = runTest {
        val err = authenticate(unreachableClient("connection refused")).leftOrFail()
        val unreachable = assertIs<McwsError.Unreachable>(err)
        assertEquals("mc.local", unreachable.host)
    }

    @Test
    fun authenticateMapsMissingTokenToParseError() = runTest {
        // Status OK but no Token item: a malformed success is a parse error,
        // not a rejection.
        val client = clientRespondingWith("""<Response Status="OK"/>""")
        assertIs<McwsError.ParseError>(authenticate(client).leftOrFail())
    }

    @Test
    fun authenticateMapsGarbageBodyToMcwsRejected() = runTest {
        // The lenient XML parser turns garbage into Status="Failure".
        val client = clientRespondingWith("this is not xml at all")
        assertIs<McwsError.McwsRejected>(authenticate(client).leftOrFail())
    }

    // --- checkAlive ---

    @Test
    fun checkAliveReturnsFriendlyNameOnOk() = runTest {
        val client = clientRespondingWith(
            """<Response Status="OK"><Item Name="FriendlyName">Living Room</Item></Response>"""
        )
        assertEquals(Either.Right("Living Room"), checkAlive(client))
    }

    @Test
    fun checkAliveDefaultsFriendlyNameWhenAbsent() = runTest {
        val client = clientRespondingWith(
            """<Response Status="OK"><Item Name="Other">x</Item></Response>"""
        )
        assertEquals(Either.Right("JRiver Server"), checkAlive(client))
    }

    @Test
    fun checkAliveMapsMcwsFailureToMcwsRejected() = runTest {
        val client = clientRespondingWith("""<Response Status="Failure"/>""")
        assertEquals(McwsError.McwsRejected("Failure"), checkAlive(client).leftOrFail())
    }

    // --- webPlayLookup ---

    @Test
    fun webPlayLookupParsesServerLocation() = runTest {
        val client = clientRespondingWith(
            """<Response><ip>1.2.3.4</ip><port>52199</port><httpsport>52200</httpsport><localiplist>10.0.0.5,10.0.0.6</localiplist></Response>"""
        )
        val result = webPlayLookup(client, "123456").rightOrFail()
        assertEquals("1.2.3.4", result.ip)
        assertEquals(52199, result.port)
        assertEquals(52200, result.httpsPort)
        assertEquals(listOf("10.0.0.5", "10.0.0.6"), result.localIpList)
    }

    @Test
    fun webPlayLookupMapsMissingIpToParseError() = runTest {
        val client = clientRespondingWith("""<Response><port>52199</port></Response>""")
        assertIs<McwsError.ParseError>(webPlayLookup(client, "123456").leftOrFail())
    }

    @Test
    fun webPlayLookupMapsHttpFailureToHttpError() = runTest {
        val client = clientRespondingWith("missing", HttpStatusCode.NotFound)
        assertEquals(McwsError.HttpError(404), webPlayLookup(client, "123456").leftOrFail())
    }

    @Test
    fun webPlayLookupMapsTransportFailureToUnreachable() = runTest {
        val err = webPlayLookup(unreachableClient("dns failure"), "123456").leftOrFail()
        assertIs<McwsError.Unreachable>(err)
    }

    // --- helpers ---

    private fun <A, B> Either<A, B>.leftOrFail(): A {
        assertTrue(isLeft(), "expected Left but was $this")
        return leftOrNull()!!
    }

    private fun <A, B> Either<A, B>.rightOrFail(): B {
        assertTrue(isRight(), "expected Right but was $this")
        return getOrNull()!!
    }
}
