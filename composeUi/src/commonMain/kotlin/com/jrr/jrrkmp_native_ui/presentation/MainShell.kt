package com.jrr.jrrkmp_native_ui.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.presentation.components.MiniPlayer
import com.jrr.jrrkmp_native_ui.presentation.navigation.LibraryComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.PlayerComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootConfig
import com.jrr.jrrkmp_native_ui.presentation.screens.*
import com.jrr.jrrkmp_native_ui.presentation.shell.LargeScreenShell
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.MainShellViewModel

/** Maps the legacy connect-flow tab index onto a typed [RootConfig]. */
private fun tabConfig(tab: Int): RootConfig = when (tab) {
    0 -> RootConfig.Library
    2 -> RootConfig.Player
    3 -> RootConfig.Zones
    4 -> RootConfig.Settings
    else -> RootConfig.Server
}

/**
 * The shared application shell, reused by the Android host and the Compose
 * Desktop host. Platform concerns are injected:
 *  - toast/share via [LocalPlatformUi]
 *  - artwork resolution via [LocalArtworkResolver]
 *  - the window width (in dp) via [windowWidthDp] — Android passes
 *    `LocalConfiguration.current.screenWidthDp`; desktop measures the window
 *    with `BoxWithConstraints` and passes `maxWidth.value.toInt()`.
 */
@Composable
fun MainShell(
    root: RootComponent,
    connectViewModel: MainShellViewModel,
    facade: AudioPlayerFacade,
    serverRepository: ServerRepository,
    windowWidthDp: Int,
) {
    val platformUi = LocalPlatformUi.current
    val mcwsClient = LocalMcwsClient.current

    val stack by root.stack.subscribeAsState()
    val active = stack.active.configuration

    val playerStatus by facade.playerStatus.collectAsState()
    val shellState by connectViewModel.state.collectAsState()

    // Scroll-to-hide chrome: a scrolling list collapses the header, the in-list
    // filter, and the mini-player to maximise the scroll area. Reset whenever
    // the active tab changes (new tab starts at the top).
    var chromeCollapsed by remember { mutableStateOf(false) }
    LaunchedEffect(active) { chromeCollapsed = false }

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
            platformUi.showToast(msg)
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

    val activeZone by facade.activeZone.collectAsState()

    // Large-screen (tablet / desktop) layout swaps the bottom tab bar for a
    // persistent left sidebar. Phones (incl. landscape) and narrow multi-window
    // stay <840dp and keep the bottom-tab Scaffold. Two large flavours:
    //  - medium (840..1200dp): narrow icon rail + single-column content.
    //  - expanded (>=1200dp): full sidebar + split-pane content.
    val isLargeScreen = windowWidthDp >= 840
    val isExpanded = windowWidthDp >= 1200

    val content: @Composable () -> Unit = {
        MainContent(
            stack = stack,
            root = root,
            facade = facade,
            serverRepository = serverRepository,
            connectViewModel = connectViewModel,
            // Split-pane content (master/detail, queue rail, two-column album,
            // two-column login) is only used on the expanded tier; medium uses
            // the phone single-column layouts inside the rail shell.
            isLargeScreen = isExpanded,
            chromeCollapsed = chromeCollapsed,
            onChromeCollapsedChange = { chromeCollapsed = it },
        )
    }

    if (isLargeScreen && active != RootConfig.Server) {
        LargeScreenShell(
            expanded = isExpanded,
            active = active,
            onSelectTab = { root.selectTab(it) },
            trackName = trackName,
            trackArtist = trackArtist,
            trackImageUrl = trackImageUrl,
            isPlaying = isPlaying,
            progress = progress,
            activeZoneName = if (activeZone.isOffline) "No Zone" else activeZone.name,
            onPlayPauseClick = { if (isPlaying) facade.pause() else facade.play() },
            onNextClick = { facade.next() },
            onPrevClick = { facade.previous() },
            onVolumeUp = { facade.setVolume(((playerStatus?.volume ?: 0.5f) + 0.05f).coerceIn(0f, 1f)) },
            onVolumeDown = { facade.setVolume(((playerStatus?.volume ?: 0.5f) - 0.05f).coerceIn(0f, 1f)) },
            chromeCollapsed = chromeCollapsed,
            content = content,
        )
    } else {
        PhoneShell(
            active = active,
            root = root,
            facade = facade,
            trackName = trackName,
            trackArtist = trackArtist,
            trackImageUrl = trackImageUrl,
            isPlaying = isPlaying,
            progress = progress,
            chromeCollapsed = chromeCollapsed,
            content = content,
        )
    }

    if (shellState.isAutoConnecting) {
        AutoConnectingOverlay(
            serverName = shellState.autoConnectServerName,
            onCancel = { connectViewModel.cancelAutoConnect() },
        )
    }
}

@Composable
private fun PhoneShell(
    active: RootConfig,
    root: RootComponent,
    facade: AudioPlayerFacade,
    trackName: String?,
    trackArtist: String?,
    trackImageUrl: String?,
    isPlaying: Boolean,
    progress: Float,
    chromeCollapsed: Boolean,
    content: @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            if (active != RootConfig.Server) {
                Column {
                    // Mini Player: above the tab bar, on every tab except Player,
                    // and only when a track is active/loaded. Hidden while a list
                    // is scrolled (chrome collapsed) to maximise the scroll area.
                    if (active != RootConfig.Player && !trackName.isNullOrEmpty() && !chromeCollapsed) {
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
                            Triple("Zones", Icons.AutoMirrored.Filled.List, RootConfig.Zones),
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
            content()
        }
    }
}

/**
 * Routed content switch, shared by the phone (bottom-tab) and large-screen
 * (sidebar) shells. Renders the active [RootComponent] child.
 */
@Composable
private fun MainContent(
    stack: com.arkivanov.decompose.router.stack.ChildStack<RootConfig, RootComponent.RootChild>,
    root: RootComponent,
    facade: AudioPlayerFacade,
    serverRepository: ServerRepository,
    connectViewModel: MainShellViewModel,
    isLargeScreen: Boolean,
    chromeCollapsed: Boolean,
    onChromeCollapsedChange: (Boolean) -> Unit,
) {
    when (val child = stack.active.instance) {
        is RootComponent.RootChild.Library -> LibraryChildren(
            component = child.component,
            isLargeScreen = isLargeScreen,
            chromeCollapsed = chromeCollapsed,
            onChromeCollapsedChange = onChromeCollapsedChange
        )
        is RootComponent.RootChild.Server -> ServerManagerScreen(
            facade = facade,
            serverRepository = serverRepository,
            onConnectSuccess = { root.onConnectSuccess() },
            isLarge = isLargeScreen
        )
        is RootComponent.RootChild.Player -> PlayerChildren(child.component, isLargeScreen = isLargeScreen)
        is RootComponent.RootChild.Zones -> ZonesScreen(
            viewModel = child.component.vm,
            onBackClick = { root.selectTab(RootConfig.Player) },
            isLarge = isLargeScreen
        )
        is RootComponent.RootChild.Settings -> SettingsScreen(
            viewModel = child.component.vm,
            onBackClick = { root.selectTab(RootConfig.Player) },
            isLarge = isLargeScreen,
            onDisconnectClick = {
                // Disconnect (when online) or Connect (when offline): tear down
                // the server connection and go to the Server screen so the user
                // can pick a server or stay offline.
                connectViewModel.disconnect()
                root.selectTab(RootConfig.Server)
            }
        )
    }
}

@Composable
private fun AutoConnectingOverlay(serverName: String?, onCancel: () -> Unit) {
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
                text = "Connecting to $serverName...",
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
                onClick = onCancel,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.text2),
                border = BorderStroke(1.dp, AppColors.line2),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel".uppercase(), style = AppTypography.chipMono, color = AppColors.text2)
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
private fun LibraryChildren(
    component: LibraryComponent,
    isLargeScreen: Boolean,
    chromeCollapsed: Boolean,
    onChromeCollapsedChange: (Boolean) -> Unit
) {
    val stack by component.stack.subscribeAsState()
    val stateHolder = rememberSaveableStateHolder()
    val active = stack.active.instance

    val stateKey = when (active) {
        is LibraryComponent.Child.List -> "list"
        is LibraryComponent.Child.Detail -> "detail:${active.album.albumGroupId}"
    }

    stateHolder.SaveableStateProvider(key = stateKey) {
        when (active) {
            is LibraryComponent.Child.List -> if (isLargeScreen) {
                LibraryLargeScreen(
                    viewModel = active.vm,
                    onAlbumClick = { album -> component.openAlbum(album) },
                )
            } else {
                LibraryScreen(
                    viewModel = active.vm,
                    onAlbumClick = { album -> component.openAlbum(album) },
                    chromeCollapsed = chromeCollapsed,
                    onChromeCollapsedChange = onChromeCollapsedChange
                )
            }
            is LibraryComponent.Child.Detail -> AlbumDetailScreen(
                viewModel = active.vm,
                onBackClick = { component.back() },
                isLarge = isLargeScreen
            )
        }
    }
}

/**
 * Player tab. On phone: NowPlaying → Queue, driven by [PlayerComponent.stack].
 * On large screens: a split with the Now Playing hero beside a persistent queue
 * rail (no queue navigation), fed by the component-level [PlayerComponent.queueViewModel].
 */
@Composable
private fun PlayerChildren(component: PlayerComponent, isLargeScreen: Boolean) {
    val stack by component.stack.subscribeAsState()

    if (isLargeScreen) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Only show the persistent queue rail (3-column layout) when the
            // content area is wide enough for both the hero and a usable rail.
            // On narrower large widths (e.g. a Pixel Fold's inner display) drop
            // to two columns — the hero fills the content and the queue opens as
            // its own screen via the header's queue button.
            val showQueueRail = maxWidth >= 720.dp
            if (showQueueRail) {
                val active = stack.active.instance
                val npVm = (active as? PlayerComponent.Child.NowPlaying)?.vm
                    ?: (active as? PlayerComponent.Child.Queue)?.let {
                        component.closeQueue(); null
                    }
                // The queue rail takes at most ~36% of the width, capped at
                // 380dp; the hero (weight) gets the rest.
                val queueWidth = minOf(380.dp, maxWidth * 0.36f)
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (npVm != null) {
                            // Queue is shown in the rail beside it, so no button.
                            NowPlayingScreen(viewModel = npVm, onQueueClick = {}, showQueueButton = false)
                        }
                    }
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(AppColors.line))
                    QueueScreen(
                        viewModel = component.queueViewModel,
                        onBackClick = {},
                        modifier = Modifier.width(queueWidth),
                        isRail = true,
                    )
                }
            } else {
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
        }
        return
    }

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
