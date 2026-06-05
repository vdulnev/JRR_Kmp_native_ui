@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.ZonesViewModel
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvListRow
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrButtonColors
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrGold
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrMuted

@Composable
fun TvZonesScreen(vm: ZonesViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refreshZones() }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 8.dp)) {
        Text("Zones", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Volume ${(s.currentVolume * 100).toInt()}%", color = JrrMuted)
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = { vm.setVolume((s.currentVolume - 0.05f).coerceIn(0f, 1f)) },
                colors = jrrButtonColors(),
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Volume down")
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { vm.setVolume((s.currentVolume + 0.05f).coerceIn(0f, 1f)) },
                colors = jrrButtonColors(),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Volume up")
            }
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item { SectionLabel("This Device") }
            items(s.deviceZones, key = { it.id }) { zone ->
                TvListRow(
                    headline = zone.name,
                    supporting = if (zone.id == s.activeZoneId) "Active" else null,
                    onClick = { vm.selectZone(zone) },
                )
            }
            if (s.serverZones.isNotEmpty()) {
                item { SectionLabel("Server Zones") }
                items(s.serverZones, key = { it.id }) { zone ->
                    TvListRow(
                        headline = zone.name,
                        supporting = if (zone.id == s.activeZoneId) "Active" else null,
                        onClick = { vm.selectZone(zone) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = JrrGold,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}
