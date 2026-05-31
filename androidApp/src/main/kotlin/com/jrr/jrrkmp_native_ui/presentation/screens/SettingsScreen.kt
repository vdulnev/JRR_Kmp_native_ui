package com.jrr.jrrkmp_native_ui.presentation.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import co.touchlab.kermit.Severity
import com.jrr.jrrkmp_native_ui.domain.model.LocalAudioQuality
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.BoxBorder
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLarge: Boolean = false
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.transientError) {
        state.transientError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearTransientError()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bg1),
        horizontalAlignment = if (isLarge) Alignment.CenterHorizontally else Alignment.Start
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
            // Large screens: cap the card column to a comfortable reading width
            // (centered by the parent's CenterHorizontally) instead of
            // stretching edge to edge.
            modifier = if (isLarge) {
                Modifier.weight(1f).widthIn(max = 760.dp)
            } else {
                Modifier.weight(1f).fillMaxWidth()
            },
            contentPadding = if (isLarge) PaddingValues(horizontal = 32.dp, vertical = 8.dp) else PaddingValues(0.dp)
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
                            if (state.isOfflineMode) {
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
                                    text = "Host: ${state.serverHost ?: ""}",
                                    style = AppTypography.itemTitle,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "Port: ${if (state.useSsl) state.serverSslPort else state.serverPort} (${if (state.useSsl) "SSL" else "HTTP"})",
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

            // Storage and Downloads Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Storage & Downloads".uppercase(),
                        style = AppTypography.sectionHeading,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bg2),
                        border = BoxBorder(AppColors.line),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Downloaded Tracks",
                                style = AppTypography.monoLabel,
                                color = AppColors.text3
                            )
                            Text(
                                text = "${state.downloadedTracksCount} Tracks cached",
                                style = AppTypography.itemTitle,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Occupies space offline for lag-free playback",
                                style = AppTypography.itemSubtitle,
                                color = AppColors.text2,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.clearDownloads()
                                },
                                enabled = state.downloadedTracksCount > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.bg0,
                                    disabledContainerColor = AppColors.bg3
                                ),
                                border = if (state.downloadedTracksCount > 0) BoxBorder(AppColors.error) else BoxBorder(AppColors.line),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "CLEAR DOWNLOADS",
                                    style = AppTypography.chipMono,
                                    color = if (state.downloadedTracksCount > 0) AppColors.error else AppColors.text3
                                )
                            }
                        }
                    }
                }
            }

            // Audio Quality Section — server-side transcode level for
            // local-zone streaming and downloads.
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Audio Quality".uppercase(),
                        style = AppTypography.sectionHeading,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bg2),
                        border = BoxBorder(AppColors.line),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "STREAMING & DOWNLOADS",
                                style = AppTypography.monoLabel,
                                color = AppColors.text3
                            )
                            Text(
                                text = "Server transcodes to this format on the fly. Lossless preserves fidelity; lossy saves bandwidth.",
                                style = AppTypography.itemSubtitle,
                                color = AppColors.text2,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            LocalAudioQuality.entries.forEachIndexed { index, quality ->
                                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                                val selected = state.localAudioQuality == quality
                                Button(
                                    onClick = { viewModel.setLocalAudioQuality(quality) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) AppColors.accent else AppColors.bg0
                                    ),
                                    border = BoxBorder(if (selected) AppColors.accent else AppColors.line),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = quality.label,
                                        style = AppTypography.chipMono,
                                        color = if (selected) AppColors.bg0 else AppColors.text
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Logging Section — Share log button + (debug only) severity selector
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Logging".uppercase(),
                        style = AppTypography.sectionHeading,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.bg2),
                        border = BoxBorder(AppColors.line),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "DEBUG LOG",
                                style = AppTypography.monoLabel,
                                color = AppColors.text3
                            )
                            Text(
                                text = "Recent activity from the in-memory ring buffer.",
                                style = AppTypography.itemSubtitle,
                                color = AppColors.text2,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            Button(
                                onClick = {
                                    val logText = viewModel.exportLogText()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, logText)
                                        putExtra(Intent.EXTRA_SUBJECT, "JRR debug log")
                                    }
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share debug log")
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg0),
                                border = BoxBorder(AppColors.accent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "SHARE LOG",
                                    style = AppTypography.chipMono,
                                    color = AppColors.accent
                                )
                            }

                            if (state.isDebugBuild) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "MIN SEVERITY",
                                    style = AppTypography.monoLabel,
                                    color = AppColors.text3
                                )
                                Text(
                                    text = "Filter level for all log writers. Dev builds only.",
                                    style = AppTypography.itemSubtitle,
                                    color = AppColors.text2,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                                )
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf(
                                        Severity.Verbose to "V",
                                        Severity.Debug to "D",
                                        Severity.Info to "I",
                                        Severity.Warn to "W",
                                        Severity.Error to "E",
                                    ).forEach { (sev, label) ->
                                        val selected = state.logSeverity == sev
                                        Button(
                                            onClick = { viewModel.setLogSeverity(sev) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selected) AppColors.accent else AppColors.bg0
                                            ),
                                            border = BoxBorder(if (selected) AppColors.accent else AppColors.line),
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                style = AppTypography.chipMono,
                                                color = if (selected) AppColors.bg0 else AppColors.text
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Active Downloads Section
            if (state.downloadJobs.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Active Downloads".uppercase(),
                            style = AppTypography.sectionHeading,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.bg2),
                            border = BoxBorder(AppColors.line),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                state.downloadJobs.forEachIndexed { index, job ->
                                    if (index > 0) {
                                        HorizontalDivider(
                                            color = AppColors.line2,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(job.name, style = AppTypography.itemTitle, maxLines = 1)
                                            val stateText = when (job.state) {
                                                "QUEUED" -> "Queued"
                                                "DOWNLOADING" -> "Downloading"
                                                "FAILED" -> "Failed"
                                                else -> job.state
                                            }
                                            Text(
                                                text = "${job.artist} · $stateText",
                                                style = AppTypography.itemSubtitle,
                                                color = AppColors.text2
                                            )
                                            if (job.state == "DOWNLOADING" && job.bytesTotal > 0) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                LinearProgressIndicator(
                                                    progress = job.bytesDownloaded.toFloat() / job.bytesTotal.toFloat(),
                                                    color = AppColors.accent,
                                                    trackColor = AppColors.bg3,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
