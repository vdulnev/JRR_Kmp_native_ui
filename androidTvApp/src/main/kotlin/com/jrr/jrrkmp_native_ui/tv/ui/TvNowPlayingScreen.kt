@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.NowPlayingViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.QueueViewModel
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvArtwork
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvListRow
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrButtonColors
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrGold
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrMuted
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrSurface

@Composable
fun TvNowPlayingScreen(vm: NowPlayingViewModel, queueVm: QueueViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    val q by queueVm.state.collectAsStateWithLifecycle()

    // Two columns: the now-playing/transport panel on the left, the playing
    // queue on the right.
    Row(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            TvArtwork(
                url = s.imageUrl.ifEmpty { null },
                modifier = Modifier.size(160.dp),
                cornerRadius = 12.dp,
            )
            Spacer(Modifier.height(12.dp))
            Text(s.activeZoneName, color = JrrGold, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(s.trackTitle, style = MaterialTheme.typography.titleLarge, maxLines = 2)
            Text(s.artistName, color = JrrMuted, style = MaterialTheme.typography.titleMedium)
            Text(s.albumTitle, color = JrrMuted, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))
            ProgressBar(vm)

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = { vm.previous() }, colors = jrrButtonColors()) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous")
                }
                Button(onClick = { if (s.isPlaying) vm.pause() else vm.play() }, colors = jrrButtonColors()) {
                    Icon(
                        imageVector = if (s.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (s.isPlaying) "Pause" else "Play",
                    )
                }
                Button(onClick = { vm.next() }, colors = jrrButtonColors()) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next")
                }
            }
        }

        Spacer(Modifier.width(32.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("Queue", color = JrrGold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                val queue = q.queueTracks
                if (queue.isEmpty()) {
                    item { TvListRow("Queue is empty", onClick = {}) }
                } else {
                    itemsIndexed(queue) { index, track ->
                        TvListRow(
                            headline = (if (index == q.activeIndex) "▶  " else "") + track.name,
                            supporting = "${track.artist} — ${track.album}",
                            onClick = { queueVm.playByIndex(index) },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Observes [NowPlayingViewModel.position] locally so the per-second playback
 * ticks recompose only this bar, not the whole now-playing layout.
 */
@Composable
private fun ProgressBar(vm: NowPlayingViewModel) {
    val position by vm.position.collectAsStateWithLifecycle()
    val positionMs = position.positionMs
    val durationMs = position.durationMs
    val fraction = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(JrrSurface),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(JrrGold),
            )
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(positionMs), color = JrrMuted, style = MaterialTheme.typography.bodySmall)
            Text(formatTime(durationMs), color = JrrMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
