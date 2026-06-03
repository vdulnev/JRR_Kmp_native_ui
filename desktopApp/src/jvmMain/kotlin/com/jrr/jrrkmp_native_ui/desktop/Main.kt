package com.jrr.jrrkmp_native_ui.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.jrr.jrrkmp_native_ui.composeui.DesktopAppRoot
import com.jrr.jrrkmp_native_ui.presentation.navigation.AppDeps
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootConfig

/**
 * Phase-0 toolchain spike entry point.
 *
 * Renders the real shared [RootComponent] in a Compose Desktop window. There is
 * no DI yet — [stubDeps] supplies throwing ViewModel factories, which is safe
 * because the navigation components build their ViewModels lazily and the spike
 * UI ([DesktopAppRoot]) never reads one. The real `DesktopAppContainer` + window
 * host arrive in Phase 2.
 */
fun main() = application {
    // Decompose's "main thread" on desktop is the AWT event-dispatch thread, NOT
    // the JVM `main` thread. Compose Desktop runs composition on the EDT, so the
    // component tree is built here inside `remember` (first composition) to
    // satisfy Decompose's main-thread check. A bare resumed LifecycleRegistry is
    // enough to render; window-bound lifecycle wiring comes with the Phase 2 host.
    val root = remember {
        val lifecycle = LifecycleRegistry()
        RootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            deps = stubDeps(),
            initialConfig = RootConfig.Player,
        ).also { lifecycle.resume() }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "JRR Desktop — Phase 0 spike",
    ) {
        DesktopAppRoot(root)
    }
}

/**
 * Throwing placeholder factories. Replaced by real factories wired to
 * `DesktopAppContainer` (database / facade / repositories) in Phase 2.
 */
private fun stubDeps(): AppDeps = AppDeps(
    libraryViewModel = { error("stub: libraryViewModel — wired in Phase 2") },
    albumDetailViewModel = { error("stub: albumDetailViewModel — wired in Phase 2") },
    nowPlayingViewModel = { error("stub: nowPlayingViewModel — wired in Phase 2") },
    queueViewModel = { error("stub: queueViewModel — wired in Phase 2") },
    zonesViewModel = { error("stub: zonesViewModel — wired in Phase 2") },
    settingsViewModel = { error("stub: settingsViewModel — wired in Phase 2") },
)
