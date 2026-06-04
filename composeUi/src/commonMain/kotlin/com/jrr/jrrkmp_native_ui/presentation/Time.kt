package com.jrr.jrrkmp_native_ui.presentation

/**
 * Wall-clock epoch milliseconds. `expect`/`actual` so shared screens stay off
 * JVM-only `System.currentTimeMillis()`. Both current targets are JVM-based, so
 * the actuals are identical; a future non-JVM target would supply its own.
 */
expect fun nowEpochMillis(): Long
