package com.jrr.jrrkmp_native_ui.presentation.navigation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.MainShellSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Pure navigation tests for the Decompose component tree. They drive the public
 * navigation API and assert on stack contents only — feature ViewModels are
 * built lazily and never accessed here, so the throwing factories below are
 * never invoked. This replaces the implicit routing logic that used to live in
 * `MainShellViewModel`.
 */
class RootComponentTest {

    /** AppDeps whose VM factories blow up if a test ever forces VM creation. */
    private fun navOnlyDeps(): AppDeps = AppDeps(
        libraryViewModel = { error("VM should not be built in a navigation test") },
        albumDetailViewModel = { error("VM should not be built in a navigation test") },
        nowPlayingViewModel = { error("VM should not be built in a navigation test") },
        queueViewModel = { error("VM should not be built in a navigation test") },
        zonesViewModel = { error("VM should not be built in a navigation test") },
        settingsViewModel = { error("VM should not be built in a navigation test") },
    )

    private class FakeSettings(
        private var lastZone: String? = null,
        private var hasServers: Boolean = false,
    ) : MainShellSettings {
        override fun getLastActiveZoneId(): String? = lastZone
        override fun setLastActiveZoneId(zoneId: String?) {
            lastZone = zoneId
        }

        override fun getHasSavedServers(): Boolean = hasServers
        override fun setHasSavedServers(hasSaved: Boolean) {
            hasServers = hasSaved
        }
    }

    private fun root(initial: RootConfig = RootConfig.Server): RootComponent {
        val lifecycle = LifecycleRegistry()
        val component = RootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            deps = navOnlyDeps(),
            initialConfig = initial,
        )
        lifecycle.resume()
        return component
    }

    private fun album(name: String, folder: String): Album = Album(
        name = name,
        albumArtist = "Artist",
        folderPath = folder,
        parentFolderPath = folder.substringBeforeLast('\\', ""),
        date = "2020",
        artworkFileKey = "",
        totalDiscs = 1,
        discNumber = 1,
    )

    private val RootComponent.activeConfig get() = stack.value.active.configuration

    private fun RootComponent.libraryComponent(): LibraryComponent {
        val child = stack.value.active.instance
        assertIs<RootComponent.RootChild.Library>(child)
        return child.component
    }

    private fun RootComponent.playerComponent(): PlayerComponent {
        val child = stack.value.active.instance
        assertIs<RootComponent.RootChild.Player>(child)
        return child.component
    }

    // ---- initial-tab selection (ports MainShellViewModel.init) ----

    @Test
    fun initialConfig_offlineZone_startsOnPlayer() {
        val cfg = RootComponent.initialConfig(FakeSettings(lastZone = "offline"))
        assertEquals(RootConfig.Player, cfg)
    }

    @Test
    fun initialConfig_savedServers_startsOnPlayer() {
        val cfg = RootComponent.initialConfig(FakeSettings(hasServers = true))
        assertEquals(RootConfig.Player, cfg)
    }

    @Test
    fun initialConfig_freshInstall_startsOnServer() {
        val cfg = RootComponent.initialConfig(FakeSettings())
        assertEquals(RootConfig.Server, cfg)
    }

    // ---- tab switching ----

    @Test
    fun selectTab_switchesActiveConfig() {
        val root = root(initial = RootConfig.Server)
        assertEquals(RootConfig.Server, root.activeConfig)

        root.selectTab(RootConfig.Zones)
        assertEquals(RootConfig.Zones, root.activeConfig)

        root.selectTab(RootConfig.Settings)
        assertEquals(RootConfig.Settings, root.activeConfig)
    }

    @Test
    fun onAutoConnected_navigatesToPlayer() {
        val root = root(initial = RootConfig.Server)
        root.onAutoConnected()
        assertEquals(RootConfig.Player, root.activeConfig)
    }

    @Test
    fun onDisconnect_navigatesToServer() {
        val root = root(initial = RootConfig.Player)
        root.onDisconnect()
        assertEquals(RootConfig.Server, root.activeConfig)
    }

    // ---- Library sub-stack: open album, preserve across tab switch, pop-to-root ----

    @Test
    fun openAlbum_pushesDetailOntoLibraryStack() {
        val root = root(initial = RootConfig.Server)
        root.selectTab(RootConfig.Library)
        val library = root.libraryComponent()
        assertEquals(1, library.stack.value.items.size)

        library.openAlbum(album("Kind of Blue", "D:\\music\\kob"))
        assertEquals(2, library.stack.value.items.size)
        assertIs<LibraryComponent.Child.Detail>(library.stack.value.active.instance)
    }

    @Test
    fun selectedAlbum_survivesSwitchingTabsAndBack() {
        val root = root(initial = RootConfig.Server)
        root.selectTab(RootConfig.Library)
        root.libraryComponent().openAlbum(album("Blue Train", "D:\\music\\bt"))

        // Leave Library, then come back — selection must persist (the
        // MainShellViewModel.kt:165-185 contract).
        root.selectTab(RootConfig.Player)
        assertEquals(RootConfig.Player, root.activeConfig)
        root.selectTab(RootConfig.Library)

        val library = root.libraryComponent()
        assertEquals(2, library.stack.value.items.size)
        assertIs<LibraryComponent.Child.Detail>(library.stack.value.active.instance)
    }

    @Test
    fun tappingActiveLibraryTab_popsDetailToRoot() {
        val root = root(initial = RootConfig.Library)
        val library = root.libraryComponent()
        library.openAlbum(album("Giant Steps", "D:\\music\\gs"))
        assertEquals(2, library.stack.value.items.size)

        // Tap the already-active Library tab => pop to list root.
        root.selectTab(RootConfig.Library)
        assertEquals(1, library.stack.value.items.size)
        assertIs<LibraryComponent.Child.List>(library.stack.value.active.instance)
    }

    @Test
    fun libraryBack_popsDetail() {
        val root = root(initial = RootConfig.Library)
        val library = root.libraryComponent()
        library.openAlbum(album("Moanin'", "D:\\music\\mn"))
        assertEquals(2, library.stack.value.items.size)

        library.back()
        assertEquals(1, library.stack.value.items.size)
        assertIs<LibraryComponent.Child.List>(library.stack.value.active.instance)
    }

    // ---- Player sub-stack: queue open/close ----

    @Test
    fun playerOpenAndCloseQueue_pushesAndPops() {
        val root = root(initial = RootConfig.Player)
        val player = root.playerComponent()
        assertEquals(1, player.stack.value.items.size)
        assertIs<PlayerComponent.Child.NowPlaying>(player.stack.value.active.instance)

        player.openQueue()
        assertEquals(2, player.stack.value.items.size)
        assertIs<PlayerComponent.Child.Queue>(player.stack.value.active.instance)

        player.closeQueue()
        assertEquals(1, player.stack.value.items.size)
        assertTrue(player.stack.value.active.instance is PlayerComponent.Child.NowPlaying)
    }
}
