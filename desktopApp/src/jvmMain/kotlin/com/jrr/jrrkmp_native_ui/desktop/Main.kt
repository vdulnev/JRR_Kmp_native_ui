package com.jrr.jrrkmp_native_ui.desktop

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.theme.JrrTheme
import com.jrr.jrrkmp_native_ui.presentation.ArtworkResolver
import com.jrr.jrrkmp_native_ui.presentation.LocalArtworkResolver
import com.jrr.jrrkmp_native_ui.presentation.LocalDatabase
import com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi
import com.jrr.jrrkmp_native_ui.presentation.MainShell
import com.jrr.jrrkmp_native_ui.presentation.PlatformUi
import com.jrr.jrrkmp_native_ui.presentation.navigation.AppDeps
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootComponent
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.AlbumDetailViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.MainShellViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.NowPlayingViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.QueueViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.SettingsViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.ZonesViewModel

/**
 * Phase-2 desktop entry point.
 *
 * Wires the real [DesktopAppContainer] (database / MCWS / repos / facade) into
 * the shared [MainShell], the same composable the Android host renders. Local
 * on-device audio is still the [DesktopPlayerEngine] stub (remote MCWS zone
 * control is fully functional); VLCJ playback lands in Phase 4.
 */
fun main() = application {
    // Decompose's "main thread" on desktop is the AWT event-dispatch thread, NOT
    // the JVM `main` thread. Compose Desktop runs composition on the EDT, so the
    // container + component tree are built here inside `remember` (first
    // composition) to satisfy Decompose's main-thread check.
    val settings = remember { DesktopSettings() }
    val container = remember { DesktopAppContainer(settings) }

    val root = remember {
        val facade = container.facade
        val database = container.database
        val mcwsClient = container.mcwsClient
        val libraryRepository = container.libraryRepository

        val deps = AppDeps(
            libraryViewModel = { LibraryViewModel(libraryRepository, facade, database) },
            albumDetailViewModel = { album ->
                AlbumDetailViewModel(album, libraryRepository, facade, database)
            },
            nowPlayingViewModel = { NowPlayingViewModel(facade, mcwsClient) },
            queueViewModel = { QueueViewModel(facade, libraryRepository, database) },
            zonesViewModel = { ZonesViewModel(facade, libraryRepository) },
            settingsViewModel = {
                SettingsViewModel(
                    facade = facade,
                    database = database,
                    clearPhysicalDownloads = { /* no desktop download cache yet */ },
                    isDebugBuild = true,
                )
            },
        )

        val lifecycle = LifecycleRegistry()
        RootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            deps = deps,
            initialConfig = RootComponent.initialConfig(settings),
        ).also { lifecycle.resume() }
    }

    val connectViewModel = remember {
        MainShellViewModel(container.facade, container.serverRepository, settings)
    }

    // Desktop platform actions. Toast → log line (no transient OS toast on
    // desktop yet); share → no-op until a Phase 5 desktop share surface exists.
    val platformUi = remember {
        object : PlatformUi {
            override fun showToast(message: String) {
                co.touchlab.kermit.Logger.withTag("ui:Desktop:Shell").i { "toast: $message" }
            }
            override fun shareText(text: String, subject: String?, chooserTitle: String?) {
                co.touchlab.kermit.Logger.withTag("ui:Desktop:Shell").i { "share (no-op): $text" }
            }
        }
    }

    // Desktop has no local artwork cache yet — resolve to the remote URL as-is.
    val artworkResolver = remember { ArtworkResolver { it } }

    Window(
        onCloseRequest = {
            // Release libvlc (no-op if playback never started) before quitting.
            container.localPlayerEngine.release()
            exitApplication()
        },
        title = "JRR Desktop",
        icon = painterResource("jrr_icon.png"),
    ) {
        JrrTheme {
            CompositionLocalProvider(
                LocalMcwsClient provides container.mcwsClient,
                LocalArtworkResolver provides artworkResolver,
                LocalPlatformUi provides platformUi,
                LocalDatabase provides container.database,
            ) {
                // The shared shell takes the window width in dp to pick its
                // layout tier; measure the window with BoxWithConstraints.
                BoxWithConstraints(modifier = Modifier) {
                    MainShell(
                        root = root,
                        connectViewModel = connectViewModel,
                        facade = container.facade,
                        serverRepository = container.serverRepository,
                        windowWidthDp = maxWidth.value.toInt(),
                    )
                }
            }
        }
    }
}
