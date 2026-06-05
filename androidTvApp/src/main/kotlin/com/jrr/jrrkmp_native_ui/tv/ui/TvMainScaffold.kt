@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowDefaults
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.tv.di.TvAppContainer
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrPillActiveColor
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrPillInactiveColor
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrTabColors
import com.jrr.jrrkmp_native_ui.tv.ui.library.ArtistsFlow
import com.jrr.jrrkmp_native_ui.tv.ui.library.BrowseFlow
import com.jrr.jrrkmp_native_ui.tv.ui.library.FavoritesFlow
import com.jrr.jrrkmp_native_ui.tv.ui.library.RandomAlbumsFlow

/**
 * Flat top-level tabs: Playing Now, then the Library sections, then Zones and
 * Settings. A single tab row over the content — no nested tab rows — so vertical
 * focus stays clean.
 */
enum class TvTab(val title: String) {
    PlayingNow("Playing Now"),
    Artists("Artists"),
    RandomAlbums("Random Albums"),
    Browse("Browse"),
    Favorites("Favorites"),
    Zones("Zones"),
    Settings("Settings"),
}

@Composable
fun TvMainScaffold(
    container: TvAppContainer,
    onDisconnect: () -> Unit,
) {
    var selected by remember { mutableIntStateOf(TvTab.PlayingNow.ordinal) }
    val tabs = remember { TvTab.entries }

    // Session-scoped ViewModels: created once per connection, stable across tab
    // switches (their polling/observation lives for the whole connected session).
    val nowPlayingVm = remember { container.makeNowPlayingViewModel() }
    val queueVm = remember { container.makeQueueViewModel() }
    val zonesVm = remember { container.makeZonesViewModel() }
    val settingsVm = remember { container.makeSettingsViewModel() }
    val libraryVm = remember { container.makeTvLibraryViewModel() }
    val connectVm = remember { container.makeTvConnectViewModel() }

    // While a library flow is drilled into an album/track list, a stray focus
    // landing on a tab must not switch sections (use Back to leave the drill).
    var drilledIn by remember { mutableStateOf(false) }
    // A drill-down click briefly blocks focus-switch during the transition.
    var suppressUntil by remember { mutableLongStateOf(0L) }
    // Switching tabs resets to that section's root.
    LaunchedEffect(selected) { drilledIn = false }
    val drill: () -> Unit = { suppressUntil = System.currentTimeMillis() + 800L }
    val onDrillChange: (Boolean) -> Unit = { drilledIn = it }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selected,
                modifier = Modifier.padding(horizontal = 48.dp, vertical = 16.dp),
                indicator = { tabPositions, doesTabRowHaveFocus ->
                    tabPositions.getOrNull(selected)?.let { pos ->
                        TabRowDefaults.UnderlinedIndicator(
                            currentTabPosition = pos,
                            doesTabRowHaveFocus = doesTabRowHaveFocus,
                            activeColor = jrrPillActiveColor,
                            inactiveColor = jrrPillInactiveColor,
                        )
                    }
                },
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selected,
                        onFocus = {
                            if (!drilledIn && System.currentTimeMillis() >= suppressUntil) selected = index
                        },
                        onClick = { selected = index },
                        colors = jrrTabColors(),
                    ) {
                        Text(
                            text = tab.title,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (tabs[selected]) {
                    TvTab.PlayingNow -> TvNowPlayingScreen(nowPlayingVm, queueVm)
                    TvTab.Artists -> ArtistsFlow(libraryVm, drill, onDrillChange)
                    TvTab.RandomAlbums -> RandomAlbumsFlow(libraryVm, drill, onDrillChange)
                    TvTab.Browse -> BrowseFlow(libraryVm, drill, onDrillChange)
                    TvTab.Favorites -> FavoritesFlow(libraryVm, drill, onDrillChange)
                    TvTab.Zones -> TvZonesScreen(zonesVm)
                    TvTab.Settings -> TvSettingsScreen(settingsVm, connectVm, onDisconnect)
                }
            }
        }
    }
}
