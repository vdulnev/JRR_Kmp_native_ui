package com.jrr.jrrkmp_native_ui.presentation.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.BoxBorder
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.Zone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    facade: AudioPlayerFacade,
    onBackClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeZone by facade.activeZone.collectAsState()

    val serverHost = facade.currentServerHost
    val isOfflineMode = activeZone.isOffline || serverHost.isNullOrEmpty()

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
                text = "Settings".uppercase(),
                style = AppTypography.monoLabel,
                color = AppColors.accent
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Active Server Info Card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Current Connection".uppercase(),
                        style = AppTypography.sectionHeading,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bg2),
                        border = BoxBorder(AppColors.line),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isOfflineMode) {
                                Text(
                                    text = "OFFLINE MODE",
                                    style = AppTypography.itemTitle,
                                    color = AppColors.accent
                                )
                                Text(
                                    text = "Playing cached and local files only",
                                    style = AppTypography.itemSubtitle,
                                    color = AppColors.text2,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                                )
                                Button(
                                    onClick = onDisconnectClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("CONNECT TO SERVER", style = AppTypography.chipMono, color = AppColors.bg0)
                                }
                            } else {
                                Text(
                                    text = "Connected Server",
                                    style = AppTypography.monoLabel,
                                    color = AppColors.text3
                                )
                                Text(
                                    text = "Host: $serverHost",
                                    style = AppTypography.itemTitle,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "Port: ${if (facade.currentServerUseSsl) facade.currentServerSslPort else facade.currentServerPort} (${if (facade.currentServerUseSsl) "SSL" else "HTTP"})",
                                    style = AppTypography.itemSubtitle,
                                    color = AppColors.text2,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Button(
                                    onClick = onDisconnectClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg0),
                                    border = BoxBorder(AppColors.error),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("DISCONNECT / CHANGE SERVER", style = AppTypography.chipMono, color = AppColors.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
