package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import kotlinx.coroutines.launch

@Composable
fun ZonesScreen(
    facade: AudioPlayerFacade,
    libraryRepository: LibraryRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val activeZone by facade.activeZone.collectAsState()
    val playerStatus by facade.playerStatus.collectAsState()

    var serverZones by remember { mutableStateOf<List<Zone>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val isOfflineMode = activeZone.isOffline || facade.currentServerHost.isNullOrEmpty()

    LaunchedEffect(Unit) {
        if (!isOfflineMode) {
            isLoading = true
            scope.launch {
                try {
                    serverZones = libraryRepository.getZones()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bg1)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.text
                )
            }

            Text(
                text = "Zones".uppercase(),
                style = AppTypography.monoLabel,
                color = AppColors.accent
            )

            // Placeholder to align title
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Server Zones
            if (!isOfflineMode) {
                item {
                    Text(
                        text = "Server Outputs".uppercase(),
                        style = AppTypography.sectionHeading,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppColors.accent)
                        }
                    }
                } else if (serverZones.isEmpty()) {
                    item {
                        Text(
                            text = "No server zones found.",
                            style = AppTypography.itemSubtitle,
                            color = AppColors.text3,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                } else {
                    items(serverZones) { zone ->
                        val isActive = zone.id == activeZone.id
                        ZoneRow(
                            zone = zone,
                            isActive = isActive,
                            volume = if (isActive) playerStatus?.volume ?: 0.5f else 0.5f,
                            onZoneClick = { facade.setZone(zone) },
                            onVolumeChange = { facade.setVolume(it) }
                        )
                    }
                }
            }

            // Local Device Zones
            item {
                Text(
                    text = "On Device".uppercase(),
                    style = AppTypography.sectionHeading,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            val deviceZones = mutableListOf(Zone.Local)
            deviceZones.add(Zone.Offline)
            // Add Android Auto if supported
            deviceZones.add(Zone.AndroidAuto)

            items(deviceZones) { zone ->
                val isActive = zone.id == activeZone.id
                ZoneRow(
                    zone = zone,
                    isActive = isActive,
                    volume = if (isActive) playerStatus?.volume ?: 0.5f else 0.5f,
                    onZoneClick = { facade.setZone(zone) },
                    onVolumeChange = { facade.setVolume(it) }
                )
            }
        }
    }
}

@Composable
fun ZoneRow(
    zone: Zone,
    isActive: Boolean,
    volume: Float,
    onZoneClick: () -> Unit,
    onVolumeChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) AppColors.bg3 else AppColors.bg2)
            .border(
                width = 1.dp,
                color = if (isActive) AppColors.accent else AppColors.line,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onZoneClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = zone.name,
                    style = AppTypography.itemTitle,
                    color = if (isActive) AppColors.accent else AppColors.text
                )
                Text(
                    text = when {
                        zone.isLocal -> "Local Playback"
                        zone.isOffline -> "Offline Library"
                        zone.isAndroidAuto -> "Car System"
                        zone.isDLNA -> "DLNA Renderer"
                        else -> "Network Zone"
                    },
                    style = AppTypography.itemSubtitle,
                    color = AppColors.text2
                )
            }

            if (isActive) {
                Text(
                    text = "ACTIVE",
                    style = AppTypography.monoLabel.copy(fontSize = 11.sp),
                    color = AppColors.accent
                )
            }
        }

        // Inline volume slider for active zone
        if (isActive) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔊",
                    fontSize = 16.sp,
                    color = AppColors.accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    colors = SliderDefaults.colors(
                        thumbColor = AppColors.accent,
                        activeTrackColor = AppColors.accent,
                        inactiveTrackColor = AppColors.line
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
