package com.jrr.jrrkmp_native_ui.core.logging

import co.touchlab.kermit.Severity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RingBufferLogWriterTest {

    @BeforeTest
    fun reset() = RingBufferLogWriter.clear()

    @AfterTest
    fun cleanup() = RingBufferLogWriter.clear()

    @Test
    fun appendsFormattedLine() {
        RingBufferLogWriter.log(Severity.Info, "hello", "test", null)

        val lines = RingBufferLogWriter.snapshot()
        assertEquals(1, lines.size)
        assertTrue(lines[0].startsWith("I "), "expected severity prefix, got: ${lines[0]}")
        assertTrue("[test]" in lines[0])
        assertTrue("hello" in lines[0])
    }

    @Test
    fun includesStackTraceWhenThrowableProvided() {
        val ex = RuntimeException("boom")
        RingBufferLogWriter.log(Severity.Error, "failed", "test", ex)

        val line = RingBufferLogWriter.snapshot().single()
        assertTrue("E " in line.take(2))
        assertTrue("failed" in line)
        assertTrue("RuntimeException" in line)
        assertTrue("boom" in line)
    }

    @Test
    fun ringOverflowsAtCapacity() {
        repeat(1100) { i ->
            RingBufferLogWriter.log(Severity.Verbose, "msg-$i", "t", null)
        }

        val lines = RingBufferLogWriter.snapshot()
        assertEquals(1000, lines.size)
        // Oldest entries dropped; newest preserved.
        assertTrue("msg-100" in lines.first()) // index 100 is the first kept
        assertTrue("msg-1099" in lines.last())
    }

    @Test
    fun snapshotIsIndependentOfFurtherLogs() {
        RingBufferLogWriter.log(Severity.Info, "first", "t", null)
        val snap = RingBufferLogWriter.snapshot()
        RingBufferLogWriter.log(Severity.Info, "second", "t", null)

        assertEquals(1, snap.size, "snapshot should be a copy, not a live view")
    }
}
