package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.domain.model.ArtistInfo
import com.jrr.jrrkmp_native_ui.domain.model.DiscographyAlbum
import com.jrr.jrrkmp_native_ui.presentation.LocalPlatformUi
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.ArtistInfoState

@Composable
internal fun ArtistInfoDialog(
    artistName: String,
    artistInfoState: ArtistInfoState,
    onLoad: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", style = AppTypography.chipMono, color = AppColors.accent)
            }
        },
        containerColor = AppColors.bg1,
        text = {
            // A full discography easily runs past the dialog height, so the body
            // scrolls inside the fixed-height dialog rather than being clipped.
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ArtistInfoBlock(
                    artistName = artistName,
                    artistInfoState = artistInfoState,
                    onLoad = onLoad,
                    // The dialog has a single action and the VM already forces a
                    // re-fetch for it, so load and refresh are the same call here.
                    onRefresh = onLoad,
                )
            }
        },
    )
}

@Composable
internal fun ArtistInfoBlock(
    artistName: String,
    artistInfoState: ArtistInfoState,
    /** First look at this artist — served from the cache when one is stored. */
    onLoad: () -> Unit,
    /** Re-ask the model and replace the cached profile. */
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val platformUi = LocalPlatformUi.current
    // LocalClipboardManager is deprecated in favor of the suspend Clipboard
    // API, but Compose Multiplatform 1.11 has no commonMain ClipEntry factory
    // yet, so plain-text copies can't migrate in shared code (same as
    // InfoDialog).
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.bg2)
            .border(1.dp, AppColors.line2, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Artist AI".uppercase(),
            style = AppTypography.sectionHeading,
            color = AppColors.accent,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = artistName,
            style = AppTypography.itemTitle,
            maxLines = 1,
        )

        when (artistInfoState) {
            ArtistInfoState.Idle -> {
                Text(
                    text = "Get the full story: career history and every album they released.",
                    style = AppTypography.itemSubtitle,
                    color = AppColors.text2,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
                Button(
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg0),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("GET INFO", style = AppTypography.chipMono, color = AppColors.accent)
                }
            }
            ArtistInfoState.Loading -> {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = AppColors.accent,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Researching the discography…",
                        style = AppTypography.itemSubtitle,
                        color = AppColors.text2,
                    )
                }
            }
            is ArtistInfoState.Error -> {
                Text(
                    text = artistInfoState.message,
                    style = AppTypography.itemSubtitle,
                    color = AppColors.error,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                Button(
                    // Force a re-fetch: after a failed refresh, quietly handing
                    // back the stale cached profile would look like a no-op.
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg0),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RETRY", style = AppTypography.chipMono, color = AppColors.accent)
                }
            }
            is ArtistInfoState.Success -> {
                val info = artistInfoState.info
                // Drag-select any passage; the buttons stay outside the
                // container so a long-press on them still reads as a click.
                SelectionContainer {
                    Column { ArtistInfoContent(info) }
                }
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(info.plainText()))
                            platformUi.showToast("Copied artist info")
                        },
                    ) {
                        Text("COPY", style = AppTypography.chipMono, color = AppColors.accent)
                    }
                    TextButton(onClick = onRefresh) {
                        Text("REFRESH", style = AppTypography.chipMono, color = AppColors.accent)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistInfoContent(info: ArtistInfo) {
    if (info.summaryLine.isNotBlank()) {
        Text(
            text = info.summaryLine,
            style = AppTypography.monoLabel,
            color = AppColors.text3,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    // The model writes the biography as blank-line-separated paragraphs; keep
    // that structure instead of rendering one dense wall of text.
    info.biography.split("\n\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .forEach { paragraph ->
            Text(
                text = paragraph,
                style = AppTypography.itemSubtitle,
                color = AppColors.text2,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

    if (info.discography.isEmpty()) return

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "Discography · ${info.discography.size} releases".uppercase(),
        style = AppTypography.monoLabel,
        color = AppColors.text3
    )

    info.discography.forEachIndexed { index, album ->
        if (index > 0) {
            HorizontalDivider(
                color = AppColors.line2,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        DiscographyRow(album)
    }
}

@Composable
private fun DiscographyRow(album: DiscographyAlbum) {
    Row(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = album.year.ifBlank { "—" },
            style = AppTypography.monoLabel,
            color = AppColors.accent,
            modifier = Modifier.width(48.dp)
        )
        Column {
            Text(text = album.title, style = AppTypography.itemTitle, color = AppColors.text)
            if (album.kind.isNotBlank()) {
                Text(
                    text = album.kind.uppercase(),
                    style = AppTypography.monoLabel,
                    color = AppColors.text3,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (album.history.isNotBlank()) {
                Text(
                    text = album.history,
                    style = AppTypography.itemSubtitle,
                    color = AppColors.text2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (album.insight.isNotBlank()) {
                Text(
                    text = album.insight,
                    style = AppTypography.itemSubtitle.copy(fontStyle = FontStyle.Italic),
                    color = AppColors.text3,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
