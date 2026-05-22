package com.jrr.jrrkmp_native_ui.util

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FlowObserver<T>(
    private val flow: Flow<T>
) {
    fun start(
        onEach: (T) -> Unit
    ): Disposable {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val job = flow
            .onEach { onEach(it) }
            .launchIn(scope)
        return Disposable { job.cancel() }
    }
}

fun interface Disposable {
    fun dispose()
}
