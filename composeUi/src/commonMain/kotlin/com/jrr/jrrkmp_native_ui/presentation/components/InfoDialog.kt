package com.jrr.jrrkmp_native_ui.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Album

fun Track.toInfoFields(): List<Pair<String, String>> = listOfNotNull(
    "Title" to name,
    "Artist" to artist,
    "Album" to album,
    "Album Artist" to albumArtist,
    "Date" to date,
    "Genre" to genre,
    "Track Number" to trackNumber.toString(),
    "Disc Number" to discNumber.toString(),
    "Total Tracks" to totalTracks.toString(),
    "Total Discs" to totalDiscs.toString(),
    "Duration" to formatTrackDuration(durationMs),
    "Bitrate" to if (bitrate > 0) "$bitrate kbps" else "",
    "Bit Depth" to if (bitDepth > 0) "$bitDepth bit" else "",
    "Sample Rate" to if (sampleRate > 0) "$sampleRate Hz" else "",
    "Channels" to if (channels > 0) channels.toString() else "",
    "File Type" to fileType,
    "File Path" to filePath,
    "Folder Path" to folderPath,
    "Play Count" to numberPlays.toString(),
    "File Key" to fileKey
).filter { it.second.isNotEmpty() && it.second != "0" }

private fun formatTrackDuration(durationMs: Long): String {
    val durationSec = durationMs / 1000
    return "${durationSec / 60}:${(durationSec % 60).toString().padStart(2, '0')}"
}

fun Album.toInfoFields(): List<Pair<String, String>> = listOfNotNull(
    "Name" to name,
    "Album Artist" to albumArtist,
    "Folder Path" to folderPath,
    "Parent Folder Path" to parentFolderPath,
    "Date" to date,
    "Artwork File Key" to artworkFileKey,
    "Total Discs" to totalDiscs.toString(),
    "Disc Number" to discNumber.toString()
).filter { it.second.isNotEmpty() && it.second != "0" }

@Composable
fun InfoDialog(
    title: String,
    fields: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    val platformUi = com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi.current
    // LocalClipboardManager is deprecated in favor of the suspend Clipboard
    // API, but Compose Multiplatform 1.11 has no commonMain ClipEntry
    // factory yet, so plain-text copies can't migrate in shared code.
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    val allText = fields.joinToString("\n") { "${it.first}: ${it.second}" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bg2),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        style = AppTypography.monoLabel,
                        color = AppColors.accent
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppColors.text
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable fields list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(fields) { (label, value) ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(value))
                                    platformUi.showToast("Copied: $value")
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = label.uppercase(),
                                style = AppTypography.monoLabel.copy(fontSize = 8.sp, color = AppColors.text3)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = value,
                                    style = AppTypography.itemTitle,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy field",
                                    tint = AppColors.text3.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = AppColors.line.copy(alpha = 0.5f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(allText))
                            platformUi.showToast("Copied all info")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg3),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy All", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            platformUi.shareText(text = allText, chooserTitle = "Share Info")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg3),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
