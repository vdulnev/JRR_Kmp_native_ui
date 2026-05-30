package com.jrr.jrrkmp_native_ui

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.JrrTheme
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.presentation.components.MiniPlayer
import com.jrr.jrrkmp_native_ui.presentation.navigation.AppDeps
import com.jrr.jrrkmp_native_ui.presentation.navigation.LibraryComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.PlayerComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootConfig
import com.jrr.jrrkmp_native_ui.presentation.screens.*
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.*
import android.widget.Toast
import androidx.compose.foundation.BorderStroke

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
            libraryViewModel = { LibraryViewModel(libraryRepository, facade) },
            albumDetailViewModel = { album ->
                AlbumDetailViewModel(album, libraryRepository, facade, database)
            },
            nowPlayingViewModel = { NowPlayingViewModel(facade, mcwsClient) },
            queueViewModel = { QueueViewModel(facade, libraryRepository) },
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

        setContent {
            JrrTheme {
                CompositionLocalProvider(LocalMcwsClient provides mcwsClient) {
                    MainShell(
                        root = root,
                        connectViewModel = connectViewModel,
                        facade = facade,
                        serverRepository = serverRepository,
                    )
                }
            }
        }
    }
}

/** Maps the legacy connect-flow tab index onto a typed [RootConfig]. */
private fun tabConfig(tab: Int): RootConfig = when (tab) {
    0 -> RootConfig.Library
    2 -> RootConfig.Player
    3 -> RootConfig.Zones
    4 -> RootConfig.Settings
    else -> RootConfig.Server
}

@Composable
fun MainShell(
    root: RootComponent,
    connectViewModel: MainShellViewModel,
    facade: AudioPlayerFacade,
    serverRepository: ServerRepository,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val mcwsClient = LocalMcwsClient.current

    val stack by root.stack.subscribeAsState()
    val active = stack.active.configuration

    val playerStatus by facade.playerStatus.collectAsState()
    val shellState by connectViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        connectViewModel.performAutoConnect()
    }

    // Bridge connect-driven tab changes (auto-connect success/failure, cancel)
    // into the component tree. Manual tab taps go straight to root.selectTab and
    // never touch connectViewModel, so this only fires for connection events.
    LaunchedEffect(shellState.activeTab) {
        root.selectTab(tabConfig(shellState.activeTab))
    }

    LaunchedEffect(shellState.toastMessage) {
        shellState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            connectViewModel.clearToast()
        }
    }

    val trackName = playerStatus?.trackName
    val trackArtist = playerStatus?.trackArtist
    val trackImageUrl = playerStatus?.trackFileKey
        ?.takeIf { it.isNotEmpty() }
        ?.let { mcwsClient.buildImageUrl(it) }
        ?.takeIf { it.isNotEmpty() }
    val isPlaying = playerStatus?.state == PlaybackState.PLAYING
    val duration = playerStatus?.durationMs ?: 0L
    val position = playerStatus?.positionMs ?: 0L
    val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f

    Scaffold(
        bottomBar = {
            if (active != RootConfig.Server) {
                Column {
                    // Mini Player: above the tab bar, on every tab except Player,
                    // and only when a track is active/loaded.
                    if (active != RootConfig.Player && !trackName.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                        ) {
                            MiniPlayer(
                                title = trackName,
                                artist = trackArtist ?: "",
                                imageUrl = trackImageUrl,
                                isPlaying = isPlaying,
                                progress = progress,
                                onPlayPauseClick = {
                                    if (isPlaying) facade.pause() else facade.play()
                                },
                                onNextClick = { facade.next() },
                                onPrevClick = { facade.previous() },
                                onBodyClick = {
                                    root.selectTab(RootConfig.Player)
                                }
                            )
                        }
                    }

                    // Premium Custom Tab Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .background(AppColors.bg2)
                            .drawBehind {
                                drawLine(
                                    color = AppColors.line2,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = 1.dp.toPx()
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val tabs = listOf(
                            Triple("Player", Icons.Default.PlayArrow, RootConfig.Player),
                            Triple("Library", Icons.Default.Home, RootConfig.Library),
                            Triple("Zones", Icons.Default.List, RootConfig.Zones),
                            Triple("Settings", Icons.Default.Settings, RootConfig.Settings)
                        )

                        tabs.forEach { (label, icon, config) ->
                            val selected = active == config

                            val tintColor by animateColorAsState(
                                targetValue = if (selected) AppColors.accent else AppColors.text3,
                                animationSpec = tween(durationMillis = 200),
                                label = "tabTintColor"
                            )

                            val scale by animateFloatAsState(
                                targetValue = if (selected) 1.12f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = 0.5f,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "tabScale"
                            )

                            val indicatorWidth by animateDpAsState(
                                targetValue = if (selected) 12.dp else 0.dp,
                                animationSpec = spring(
                                    dampingRatio = 0.6f,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "tabIndicatorWidth"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        root.selectTab(config)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = tintColor,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(scale)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = AppTypography.monoLabel.copy(
                                        color = tintColor,
                                        fontSize = 9.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        letterSpacing = 1.0.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(indicatorWidth)
                                        .height(2.5.dp)
                                        .background(
                                            color = AppColors.accent,
                                            shape = RoundedCornerShape(1.25.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.bg1)
                .padding(innerPadding)
        ) {
            when (val child = stack.active.instance) {
                is RootComponent.RootChild.Library -> LibraryChildren(child.component)
                is RootComponent.RootChild.Server -> ServerManagerScreen(
                    facade = facade,
                    serverRepository = serverRepository,
                    onConnectSuccess = { root.onConnectSuccess() }
                )
                is RootComponent.RootChild.Player -> PlayerChildren(child.component)
                is RootComponent.RootChild.Zones -> ZonesScreen(
                    viewModel = child.component.vm,
                    onBackClick = { root.selectTab(RootConfig.Player) }
                )
                is RootComponent.RootChild.Settings -> SettingsScreen(
                    viewModel = child.component.vm,
                    onBackClick = { root.selectTab(RootConfig.Player) },
                    onDisconnectClick = {
                        // Disconnect (when online) or Connect (when offline):
                        // tear down the server connection and go to the Server
                        // screen so the user can pick a server or stay offline.
                        connectViewModel.disconnect()
                        root.selectTab(RootConfig.Server)
                    }
                )
            }
        }
    }

    if (shellState.isAutoConnecting) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.bg0.copy(alpha = 0.95f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "JRiver Remote".uppercase(),
                    style = AppTypography.screenTitle.copy(color = AppColors.accent, fontSize = 24.sp),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Connecting to ${shellState.autoConnectServerName}...",
                    style = AppTypography.itemSubtitle,
                    color = AppColors.text2,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                CircularProgressIndicator(
                    color = AppColors.accent,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = {
                        connectViewModel.cancelAutoConnect()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.text2),
                    border = BorderStroke(1.dp, AppColors.line2),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel".uppercase(), style = AppTypography.chipMono, color = AppColors.text2)
                }
            }
        }
    }
}

/**
 * Library tab: List → AlbumDetail, driven by [LibraryComponent.stack].
 *
 * Only the active child is composed, but each child's saveable UI state is
 * retained across navigation via a [rememberSaveableStateHolder]. The scroll
 * positions inside [LibraryScreen] (artists / artist-albums / random grid) are
 * `rememberLazyListState`/`rememberLazyGridState`, which are `rememberSaveable`
 * backed — so when an album detail is pushed and later popped, the list's scroll
 * is saved while it's off-screen and restored on the way back, without keeping
 * it composed behind the detail. Keyed by child so List and Detail get
 * independent state slots (commit d2935a1 behaviour, the idiomatic way).
 */
@Composable
private fun LibraryChildren(component: LibraryComponent) {
    val stack by component.stack.subscribeAsState()
    val stateHolder = rememberSaveableStateHolder()
    val active = stack.active.instance

    val stateKey = when (active) {
        is LibraryComponent.Child.List -> "list"
        is LibraryComponent.Child.Detail -> "detail:${active.album.albumGroupId}"
    }

    stateHolder.SaveableStateProvider(key = stateKey) {
        when (active) {
            is LibraryComponent.Child.List -> LibraryScreen(
                viewModel = active.vm,
                onAlbumClick = { album -> component.openAlbum(album) }
            )
            is LibraryComponent.Child.Detail -> AlbumDetailScreen(
                viewModel = active.vm,
                onBackClick = { component.back() }
            )
        }
    }
}

/** Player tab: NowPlaying → Queue, driven by [PlayerComponent.stack]. */
@Composable
private fun PlayerChildren(component: PlayerComponent) {
    val stack by component.stack.subscribeAsState()
    when (val child = stack.active.instance) {
        is PlayerComponent.Child.NowPlaying -> NowPlayingScreen(
            viewModel = child.vm,
            onQueueClick = { component.openQueue() }
        )
        is PlayerComponent.Child.Queue -> QueueScreen(
            viewModel = child.vm,
            onBackClick = { component.closeQueue() }
        )
    }
}
