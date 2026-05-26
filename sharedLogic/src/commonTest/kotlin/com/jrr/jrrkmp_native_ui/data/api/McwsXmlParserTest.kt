package com.jrr.jrrkmp_native_ui.data.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class McwsXmlParserTest {

    @Test
    fun testParseResponse_success() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
            <Response Status="OK">
              <Item Name="FriendlyName">My JRiver &amp; Streamer</Item>
              <Item Name="Token">abcde12345</Item>
              <Item Name="Blank"></Item>
            </Response>
        """.trimIndent()

        val parsed = parseMcwsResponse(xml)
        assertEquals("OK", parsed.status)
        assertEquals("My JRiver & Streamer", parsed.items["FriendlyName"])
        assertEquals("abcde12345", parsed.items["Token"])
        assertEquals("", parsed.items["Blank"])
    }

    @Test
    fun testParseResponse_failure() {
        val xml = """<Response Status="Failure" />"""
        val parsed = parseMcwsResponse(xml)
        assertEquals("Failure", parsed.status)
        assertTrue(parsed.items.isEmpty())
    }

    @Test
    fun testParseResponse_spacesAndSingleQuotes() {
        val xml = """<Response Status = 'OK'><Item Name = 'Key' >Value &apos;1&apos;</Item></Response>"""
        val parsed = parseMcwsResponse(xml)
        assertEquals("OK", parsed.status)
        assertEquals("Value '1'", parsed.items["Key"])
    }

    @Test
    fun testParseWebPlayLookup() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
            <Response>
              <ip>192.168.1.50</ip>
              <port>52199</port>
              <mac>00:11:22:33:44:55</mac>
            </Response>
        """.trimIndent()

        val parsed = parseMcwsWebPlayLookup(xml)
        assertEquals("192.168.1.50", parsed["ip"])
        assertEquals("52199", parsed["port"])
        assertEquals("00:11:22:33:44:55", parsed["mac"])
    }
}
