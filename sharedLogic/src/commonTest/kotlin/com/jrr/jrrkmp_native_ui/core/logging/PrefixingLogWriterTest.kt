package com.jrr.jrrkmp_native_ui.core.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PrefixingLogWriterTest {

    private class Recorder : LogWriter() {
        data class Call(val severity: Severity, val message: String, val tag: String, val throwable: Throwable?)
        val calls = mutableListOf<Call>()
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            calls += Call(severity, message, tag, throwable)
        }
    }

    @Test
    fun prependsPrefixToTagOnEveryCall() {
        val rec = Recorder()
        val writer = PrefixingLogWriter(rec, "jrr:")

        writer.log(Severity.Debug, "hello", "vm:Library", null)
        writer.log(Severity.Error, "boom", "net:Ktor", RuntimeException("x"))

        assertEquals(2, rec.calls.size)
        assertEquals("jrr:vm:Library", rec.calls[0].tag)
        assertEquals("jrr:net:Ktor", rec.calls[1].tag)
    }

    @Test
    fun preservesMessageSeverityAndThrowable() {
        val rec = Recorder()
        val writer = PrefixingLogWriter(rec, "jrr:")
        val ex = RuntimeException("boom")

        writer.log(Severity.Warn, "msg", "t", ex)

        val call = rec.calls.single()
        assertEquals(Severity.Warn, call.severity)
        assertEquals("msg", call.message)
        assertTrue(call.throwable === ex, "throwable must be passed through identity-equal")
    }

    @Test
    fun emptyTagStillGetsPrefix() {
        val rec = Recorder()
        val writer = PrefixingLogWriter(rec, "jrr:")

        writer.log(Severity.Info, "msg", "", null)

        assertEquals("jrr:", rec.calls.single().tag)
    }

    @Test
    fun nullThrowableSurvives() {
        val rec = Recorder()
        val writer = PrefixingLogWriter(rec, "jrr:")

        writer.log(Severity.Info, "msg", "t", null)

        assertNull(rec.calls.single().throwable)
    }
}
