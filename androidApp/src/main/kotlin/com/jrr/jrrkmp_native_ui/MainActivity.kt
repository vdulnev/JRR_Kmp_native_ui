package com.jrr.jrrkmp_native_ui

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import com.arkivanov.decompose.defaultComponentContext
import com.jrr.jrrkmp_native_ui.core.theme.JrrTheme
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import com.jrr.jrrkmp_native_ui.presentation.MainShell
import com.jrr.jrrkmp_native_ui.presentation.navigation.AppDeps
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootComponent
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = appContainer
        val facade = container.facade
        val serverRepository = container.serverRepository
        val libraryRepository = container.libraryRepository
        val mcwsClient = container.mcwsClient
        val database = container.database

        val prefs = getSharedPreferences("jrr_settings", Context.MODE_PRIVATE)
        val settings = object : MainShellSettings {
            override fun getLastActiveZoneId(): String? = prefs.getString("last_active_zone_id", null)
            override fun setLastActiveZoneId(zoneId: String?) = prefs.edit().putString("last_active_zone_id", zoneId).apply()
            override fun getHasSavedServers(): Boolean = prefs.getBoolean("has_saved_servers", false)
            override fun setHasSavedServers(hasSaved: Boolean) = prefs.edit().putBoolean("has_saved_servers", hasSaved).apply()
        }

        val isDebug = (applicationInfo.flags and
            android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val clearPhysicalDownloads: () -> Unit = {
            val downloadsDir = java.io.File(filesDir, "downloads")
            if (downloadsDir.exists() && downloadsDir.isDirectory) {
                downloadsDir.listFiles()?.forEach { file -> file.delete() }
            }
        }

        // Feature ViewModels are now built by the component tree, lazily and
        // retained in Essenty's InstanceKeeper (survives rotation, deterministic
        // onCleared). The host only supplies the factory lambdas.
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
                    clearPhysicalDownloads = clearPhysicalDownloads,
                    isDebugBuild = isDebug,
                )
            },
        )

        // defaultComponentContext() wires StateKeeper (savedStateRegistry),
        // BackHandler (onBackPressedDispatcher) and the Activity Lifecycle, so the
        // tab back-stack survives process death and system-back pops inner stacks.
        val root = RootComponent(
            componentContext = defaultComponentContext(),
            deps = deps,
            initialConfig = RootComponent.initialConfig(settings),
        )

        // MainShellViewModel still owns auto-connect + toast (connection business
        // logic, not navigation). Its only navigation side-effect — flip to Player
        // on connect / back to Server on failure — is bridged into the component
        // tree in MainShell below, until Phase 5 relocates it into RootComponent.
        val connectViewModel = MainShellViewModel(facade, serverRepository, settings)

        // Map artwork URLs to a locally-downloaded file when present (parses the
        // `File=<key>` param, checks filesDir/downloads/art_<key>.jpg). Ported
        // from the inline LocalContext logic that lived in MiniPlayer/VinylSleeve
        // before those moved to the shared :composeUi module.
        val artworkResolver = com.jrr.jrrkmp_native_ui.presentation.ArtworkResolver { imageUrl ->
            val fileParam = "File="
            val index = imageUrl.indexOf(fileParam)
            val fileKey = if (index != -1) {
                val start = index + fileParam.length
                val end = imageUrl.indexOf('&', start)
                if (end == -1) imageUrl.substring(start) else imageUrl.substring(start, end)
            } else null
            if (fileKey != null) {
                val artFile = java.io.File(filesDir, "downloads/art_${fileKey}.jpg")
                if (artFile.exists()) artFile else imageUrl
            } else {
                imageUrl
            }
        }

        // Platform UI actions (toast + share) used by the shared screens, which
        // no longer reference android.widget.Toast / Intent directly.
        val platformUi = object : com.jrr.jrrkmp_native_ui.presentation.PlatformUi {
            override fun showToast(message: String) {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            }

            override fun shareText(text: String, subject: String?, chooserTitle: String?) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    if (subject != null) putExtra(Intent.EXTRA_SUBJECT, subject)
                }
                startActivity(Intent.createChooser(intent, chooserTitle))
            }
        }

        setContent {
            JrrTheme {
                CompositionLocalProvider(
                    LocalMcwsClient provides mcwsClient,
                    com.jrr.jrrkmp_native_ui.presentation.LocalArtworkResolver provides artworkResolver,
                    com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi provides platformUi,
                    com.jrr.jrrkmp_native_ui.presentation.LocalDatabase provides database,
                ) {
                    MainShell(
                        root = root,
                        connectViewModel = connectViewModel,
                        facade = facade,
                        serverRepository = serverRepository,
                        windowWidthDp = LocalConfiguration.current.screenWidthDp,
                    )
                }
            }
        }
    }
}
