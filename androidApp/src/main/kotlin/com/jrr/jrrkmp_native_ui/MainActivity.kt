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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.JrrTheme
import com.jrr.jrrkmp_native_ui.core.di.LocalMcwsClient
import com.jrr.jrrkmp_native_ui.core.di.appContainer
import com.jrr.jrrkmp_native_ui.core.theme.BoxBorder
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.presentation.components.MiniPlayer
import com.jrr.jrrkmp_native_ui.presentation.screens.*
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.*
import android.widget.Toast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import com.jrr.jrrkmp_native_ui.domain.model.Album

class MainActivity : ComponentActivity() {

    private lateinit var facade: AudioPlayerFacade
    private lateinit var serverRepository: ServerRepository
    private lateinit var libraryRepository: LibraryRepository
    private lateinit var mcwsClient: McwsClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = appContainer
        facade = container.facade
        serverRepository = container.serverRepository
        libraryRepository = container.libraryRepository
        mcwsClient = container.mcwsClient

        val prefs = getSharedPreferences("jrr_settings", Context.MODE_PRIVATE)
        val settings = object : MainShellSettings {
            override fun getLastActiveZoneId(): String? = prefs.getString("last_active_zone_id", null)
            override fun setLastActiveZoneId(zoneId: String?) = prefs.edit().putString("last_active_zone_id", zoneId).apply()
            override fun getHasSavedServers(): Boolean = prefs.getBoolean("has_saved_servers", false)
            override fun setHasSavedServers(hasSaved: Boolean) = prefs.edit().putBoolean("has_saved_servers", hasSaved).apply()
        }
        val mainShellViewModel = MainShellViewModel(facade, serverRepository, settings)

        setContent {
            JrrTheme {
                CompositionLocalProvider(LocalMcwsClient provides mcwsClient) {
                    MainShell(
                        viewModel = mainShellViewModel,
                        facade = facade,
                        serverRepository = serverRepository,
                        libraryRepository = libraryRepository,
                        mcwsClient = mcwsClient,
                    )
                }
            }
        }
    }
}

@Composable
fun MainShell(
    viewModel: MainShellViewModel,
    facade: AudioPlayerFacade,
    serverRepository: ServerRepository,
    libraryRepository: LibraryRepository,
    mcwsClient: McwsClient,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    val libraryViewModel = remember {
        LibraryViewModel(libraryRepository, facade)
    }
    val nowPlayingViewModel = remember {
        NowPlayingViewModel(facade, mcwsClient)
    }
    val queueViewModel = remember {
        QueueViewModel(facade, libraryRepository, context.appContainer.database)
    }
    val zonesViewModel = remember {
        ZonesViewModel(facade, libraryRepository)
    }
    val settingsViewModel = remember {
        val isDebug = (context.applicationInfo.flags and
            android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        SettingsViewModel(
            facade = facade,
            database = context.appContainer.database,
            clearPhysicalDownloads = {
                val downloadsDir = java.io.File(context.filesDir, "downloads")
                if (downloadsDir.exists() && downloadsDir.isDirectory) {
                    downloadsDir.listFiles()?.forEach { file ->
                        file.delete()
                    }
                }
            },
            isDebugBuild = isDebug,
        )
    }

    val playerStatus by facade.playerStatus.collectAsState()
    val activeZone by facade.activeZone.collectAsState()

    val shellState by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.performAutoConnect()
    }

    LaunchedEffect(shellState.toastMessage) {
        shellState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
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
            if (shellState.activeTab != 1) {
                Column {
                    // Mini Player: sits above the tab bar, shown on all tabs except Now Playing (2),
                    // and only if a track is active/loaded
                    if (shellState.activeTab != 2 && !trackName.isNullOrEmpty()) {
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
                                    viewModel.selectTab(2) // Switch to full Player tab
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
                                // Sleek top border line
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
                            Triple("Player", Icons.Default.PlayArrow, 2),
                            Triple("Library", Icons.Default.Home, 0),
                            Triple("Zones", Icons.Default.List, 3),
                            Triple("Settings", Icons.Default.Settings, 4)
                        )

                        tabs.forEach { (label, icon, index) ->
                            val selected = shellState.activeTab == index
                            
                            // Smooth color transition
                            val tintColor by animateColorAsState(
                                targetValue = if (selected) AppColors.accent else AppColors.text3,
                                animationSpec = tween(durationMillis = 200),
                                label = "tabTintColor"
                            )
                            
                            // Smooth scale micro-animation
                            val scale by animateFloatAsState(
                                targetValue = if (selected) 1.12f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = 0.5f,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "tabScale"
                            )

                            // Smooth width transition for selection indicator pill
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
                                        indication = null // Clean animated tab clicks without clunky M3 background ripple
                                    ) {
                                        viewModel.selectTab(index)
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
                                // Subtle gold indicator dot/pill under active tab
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
            when (shellState.activeTab) {
                0 -> {
                    // Library Tab with nested AlbumDetailScreen
                    val album = shellState.selectedAlbum
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = if (album != null) {
                                Modifier.size(0.dp)
                            } else {
                                Modifier.fillMaxSize()
                            }
                        ) {
                            LibraryScreen(
                                viewModel = libraryViewModel,
                                onAlbumClick = { alb ->
                                    viewModel.selectAlbum(alb)
                                }
                            )
                        }
                        
                        if (album != null) {
                            val albumDetailViewModel = remember(album) {
                                AlbumDetailViewModel(
                                    album = album,
                                    libraryRepository = libraryRepository,
                                    facade = facade,
                                    database = context.appContainer.database
                                )
                            }
                            AlbumDetailScreen(
                                viewModel = albumDetailViewModel,
                                onBackClick = { viewModel.selectAlbum(null) }
                            )
                        }
                    }
                }
                1 -> {
                    ServerManagerScreen(
                        facade = facade,
                        serverRepository = serverRepository,
                        onConnectSuccess = { viewModel.selectTab(2) }
                    )
                }
                2 -> {
                    // Now Playing Tab with nested QueueScreen
                    if (shellState.showQueue) {
                        QueueScreen(
                            viewModel = queueViewModel,
                            onBackClick = { viewModel.setShowQueue(false) }
                        )
                    } else {
                        NowPlayingScreen(
                            viewModel = nowPlayingViewModel,
                            onQueueClick = { viewModel.setShowQueue(true) }
                        )
                    }
                }
                3 -> {
                    ZonesScreen(
                        viewModel = zonesViewModel,
                        onBackClick = { viewModel.selectTab(2) }
                    )
                }
                4 -> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onBackClick = { viewModel.selectTab(2) },
                        onDisconnectClick = {
                            viewModel.disconnect()
                        }
                    )
                }
            }
        }
    }

    if (shellState.isAutoConnecting) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.bg0.copy(alpha = 0.95f))
                .clickable(enabled = false) {}, // Scrim intercept
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
                        viewModel.cancelAutoConnect()
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