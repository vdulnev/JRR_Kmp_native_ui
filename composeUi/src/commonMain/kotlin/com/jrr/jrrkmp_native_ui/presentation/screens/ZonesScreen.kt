package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.ZonesViewModel
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun ZonesScreen(
    viewModel: ZonesViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val platformUi = com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi.current

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            platformUi.showToast(error)
            viewModel.clearTransientError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshZones()
    }

    val serverZones = state.serverZones
    val deviceZones = state.deviceZones
    val activeZoneId = state.activeZoneId
    val currentVolume = state.currentVolume
    val isLoading = state.isLoading
    val isOfflineMode = state.isOfflineMode

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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

        val gutter = if (isLarge) 32.dp else 16.dp
        if (isLarge) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(gutter),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isOfflineMode) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("Server Outputs".uppercase(), style = AppTypography.sectionHeading)
                    }
                    if (isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = AppColors.accent)
                            }
                        }
                    } else if (serverZones.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text("No server zones found.", style = AppTypography.itemSubtitle, color = AppColors.text3)
                        }
                    } else {
                        gridItems(serverZones) { zone ->
                            val isActive = zone.id == activeZoneId
                            ZoneRow(
                                zone = zone,
                                isActive = isActive,
                                volume = if (isActive) currentVolume else 0.5f,
                                onZoneClick = { viewModel.selectZone(zone) },
                                onVolumeChange = { viewModel.setVolume(it) }
                            )
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("On Device".uppercase(), style = AppTypography.sectionHeading, modifier = Modifier.padding(top = 12.dp))
                }
                gridItems(deviceZones) { zone ->
                    val isActive = zone.id == activeZoneId
                    ZoneRow(
                        zone = zone,
                        isActive = isActive,
                        volume = if (isActive) currentVolume else 0.5f,
                        onZoneClick = { viewModel.selectZone(zone) },
                        onVolumeChange = { viewModel.setVolume(it) }
                    )
                }
            }
            return@Column
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
                        val isActive = zone.id == activeZoneId
                        ZoneRow(
                            zone = zone,
                            isActive = isActive,
                            volume = if (isActive) currentVolume else 0.5f,
                            onZoneClick = { viewModel.selectZone(zone) },
                            onVolumeChange = { viewModel.setVolume(it) }
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

            items(deviceZones) { zone ->
                val isActive = zone.id == activeZoneId
                ZoneRow(
                    zone = zone,
                    isActive = isActive,
                    volume = if (isActive) currentVolume else 0.5f,
                    onZoneClick = { viewModel.selectZone(zone) },
                    onVolumeChange = { viewModel.setVolume(it) }
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
                        zone.isOffline -> "No Server Connection Required"
                        zone.isLocal -> "Local Android Player Engine"
                        zone.isAndroidAuto -> "Android Auto Mode"
                        zone.isDLNA -> "DLNA Network Renderer"
                        else -> "JRiver Media Center Zone"
                    },
                    style = AppTypography.itemSubtitle,
                    color = AppColors.text3,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (isActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.accentDim)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        style = AppTypography.monoLabel.copy(color = AppColors.accent, fontSize = 9.sp)
                    )
                }
            }
        }

        if (isActive && !zone.isOffline) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Volume".uppercase(), style = AppTypography.monoLabel, color = AppColors.text2)
                Spacer(modifier = Modifier.width(12.dp))
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
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(volume * 100).toInt()}%",
                    style = AppTypography.monoLabel,
                    color = AppColors.text,
                    modifier = Modifier.width(36.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}
