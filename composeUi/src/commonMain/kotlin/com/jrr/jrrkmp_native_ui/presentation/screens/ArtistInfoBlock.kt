package com.jrr.jrrkmp_native_ui.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
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
            ArtistInfoBlock(
                artistName = artistName,
                artistInfoState = artistInfoState,
                onLoad = onLoad,
            )
        },
    )
}

@Composable
internal fun ArtistInfoBlock(
    artistName: String,
    artistInfoState: ArtistInfoState,
    onLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    text = "Get a short bio and essential albums.",
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
                    Text("Loading artist info", style = AppTypography.itemSubtitle, color = AppColors.text2)
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
                    onClick = onLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.bg0),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("RETRY", style = AppTypography.chipMono, color = AppColors.accent)
                }
            }
            is ArtistInfoState.Success -> {
                val info = artistInfoState.info
                Text(
                    text = info.shortBio,
                    style = AppTypography.itemSubtitle,
                    color = AppColors.text2,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Best albums".uppercase(),
                    style = AppTypography.monoLabel,
                    color = AppColors.text3
                )
                info.bestAlbums.forEach { album ->
                    Text(
                        text = album,
                        style = AppTypography.itemTitle,
                        color = AppColors.text,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                TextButton(onClick = onLoad, modifier = Modifier.align(Alignment.End)) {
                    Text("REFRESH", style = AppTypography.chipMono, color = AppColors.accent)
                }
            }
        }
    }
}
