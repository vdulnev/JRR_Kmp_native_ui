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
import com.jrr.jrrkmp_native_ui.core.theme.BoxBorder
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.presentation.components.MiniPlayer
import com.jrr.jrrkmp_native_ui.presentation.screens.*
import android.widget.Toast
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke

class MainActivity : ComponentActivity() {

    private lateinit var facade: AudioPlayerFacade
    private lateinit var serverRepository: ServerRepository
    private lateinit var libraryRepository: LibraryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        facade = JrrDependencies.getAudioPlayerFacade(this)
        serverRepository = JrrDependencies.getServerRepository(this)
        libraryRepository = JrrDependencies.getLibraryRepository(this)

        setContent {
            JrrTheme {
                MainShell(
                    facade = facade,
                    serverRepository = serverRepository,
                    libraryRepository = libraryRepository
                )
            }
        }
    }
}

@Composable
fun MainShell(
    facade: AudioPlayerFacade,
    serverRepository: ServerRepository,
    libraryRepository: LibraryRepository
) {
    val playerStatus by facade.playerStatus.collectAsState()
    val activeZone by facade.activeZone.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("jrr_settings", Context.MODE_PRIVATE) }
    val lastActiveZoneId = remember { prefs.getString("last_active_zone_id", null) }
    val hasSavedServers = remember { prefs.getBoolean("has_saved_servers", false) }

    // Tab state: 0 = Library, 1 = Server Manager, 2 = Now Playing, 3 = Zones, 4 = Settings
    var activeTab by remember {
        mutableStateOf(
            if (lastActiveZoneId == Zone.Offline.id) 2
            else if (hasSavedServers) 2
            else 1
        )
    }

    // Navigation sub-states
    var selectedAlbum by remember { mutableStateOf<Pair<String, String>?>(null) } // AlbumName to ArtistName
    var showQueue by remember { mutableStateOf(false) }

    // Auto-login states
    var isAutoConnecting by remember { mutableStateOf(false) }
    var autoConnectServerName by remember { mutableStateOf("") }
    var autoConnectJob by remember { mutableStateOf<Job?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val lastServer = serverRepository.getLastUsedServer()
        // Sync has_saved_servers with DB state in case of migration
        prefs.edit().putBoolean("has_saved_servers", lastServer != null).apply()

        if (lastActiveZoneId == Zone.Offline.id) {
            facade.setZone(Zone.Offline)
            activeTab = 2
            return@LaunchedEffect
        }

        if (lastServer != null) {
            autoConnectServerName = lastServer.friendlyName ?: "JRiver Server"
            isAutoConnecting = true
            autoConnectJob = scope.launch {
                try {
                    val token = serverRepository.authenticate(
                        lastServer.host,
                        lastServer.port,
                        lastServer.useSsl,
                        lastServer.sslPort,
                        lastServer.username,
                        lastServer.passwordKey
                    )
                    if (token != null) {
                        val finalName = serverRepository.checkAlive(
                            lastServer.host,
                            lastServer.port,
                            lastServer.useSsl,
                            lastServer.sslPort,
                            token
                        ) ?: lastServer.friendlyName ?: "JRiver Server"

                        val updatedServer = lastServer.copy(
                            friendlyName = finalName,
                            lastUsedAt = System.currentTimeMillis(),
                            authToken = token
                        )
                        serverRepository.saveServer(updatedServer)

                        facade.setServerConnection(
                            lastServer.host,
                            lastServer.port,
                            lastServer.useSsl,
                            lastServer.sslPort,
                            token
                        )
                        facade.setZone(Zone.Local)

                        Toast.makeText(context, "Connected to $finalName", Toast.LENGTH_SHORT).show()
                        activeTab = 2 // Switch to Player
                    } else {
                        Toast.makeText(context, "Auto-connect failed: Authentication error", Toast.LENGTH_SHORT).show()
                        activeTab = 1
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Auto-connect failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    activeTab = 1
                } finally {
                    isAutoConnecting = false
                    autoConnectJob = null
                }
            }
        } else {
            activeTab = 1
        }
    }

    val track = playerStatus?.trackInfo
    val isPlaying = playerStatus?.state == PlaybackState.PLAYING
    val duration = playerStatus?.durationMs ?: 0L
    val position = playerStatus?.positionMs ?: 0L
    val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f

    val isServerConnected = activeZone != Zone.Offline && !facade.currentServerHost.isNullOrEmpty()

    // Automatically switch to Player once a server is connected successfully
    val onConnectSuccess = {
        activeTab = 2
    }

    Scaffold(
        bottomBar = {
            if (activeTab != 1) {
                Column {
                    // Mini Player: sits above the tab bar, shown on all tabs except Now Playing (2),
                    // and only if a track is active/loaded
                    if (activeTab != 2 && track != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp, end = 8.dp, bottom = 4.dp)
                        ) {
                            MiniPlayer(
                                title = track.name,
                                artist = track.artist,
                                imageUrl = track.imageUrl.ifEmpty { null },
                                isPlaying = isPlaying,
                                progress = progress,
                                onPlayPauseClick = {
                                    if (isPlaying) facade.pause() else facade.play()
                                },
                                onNextClick = { facade.next() },
                                onPrevClick = { facade.previous() },
                                onBodyClick = {
                                    activeTab = 2 // Switch to full Player tab
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
                            val selected = activeTab == index
                            
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
                                        activeTab = index
                                        if (index == 0) {
                                            selectedAlbum = null
                                        }
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
            when (activeTab) {
                0 -> {
                    // Library Tab with nested AlbumDetailScreen
                    val album = selectedAlbum
                    if (album != null) {
                        AlbumDetailScreen(
                            albumName = album.first,
                            artistName = album.second,
                            facade = facade,
                            libraryRepository = libraryRepository,
                            onBackClick = { selectedAlbum = null }
                        )
                    } else {
                        LibraryScreen(
                            facade = facade,
                            libraryRepository = libraryRepository,
                            onAlbumClick = { albumName, artistName ->
                                selectedAlbum = Pair(albumName, artistName)
                            }
                        )
                    }
                }
                1 -> {
                    ServerManagerScreen(
                        facade = facade,
                        serverRepository = serverRepository,
                        onConnectSuccess = onConnectSuccess
                    )
                }
                2 -> {
                    // Now Playing Tab with nested QueueScreen
                    if (showQueue) {
                        QueueScreen(
                            facade = facade,
                            libraryRepository = libraryRepository,
                            onBackClick = { showQueue = false }
                        )
                    } else {
                        NowPlayingScreen(
                            facade = facade,
                            onQueueClick = { showQueue = true }
                        )
                    }
                }
                3 -> {
                    ZonesScreen(
                        facade = facade,
                        libraryRepository = libraryRepository,
                        onBackClick = { activeTab = 2 }
                    )
                }
                4 -> {
                    SettingsScreen(
                        facade = facade,
                        onBackClick = { activeTab = 2 },
                        onDisconnectClick = {
                            facade.setServerConnection("", 0, false, 0, null)
                            facade.setZone(Zone.Offline)
                            activeTab = 1
                        }
                    )
                }
            }
        }
    }

    if (isAutoConnecting) {
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
                    text = "Connecting to $autoConnectServerName...",
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
                        autoConnectJob?.cancel()
                        isAutoConnecting = false
                        autoConnectJob = null
                        Toast.makeText(context, "Connection cancelled", Toast.LENGTH_SHORT).show()
                        activeTab = 1
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