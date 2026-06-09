@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import com.jrr.jrrkmp_native_ui.data.repository.ServerGroup
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.TvConnectViewModel
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvTextField
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrButtonColors
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrError
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrMuted
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

/**
 * Server connection form. Authenticates over MCWS, then connects through the
 * facade (which sets the active server AND switches Offline → Local so the app
 * comes online), and persists the server for launch-time restore.
 */
@Composable
fun TvConnectScreen(
    vm: TvConnectViewModel,
    onConnected: () -> Unit,
) {
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("52199") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var savedGroups by remember { mutableStateOf<List<ServerGroup>>(emptyList()) }
    // Profile awaiting a "same server as…" target pick.
    var mergeProfile by remember { mutableStateOf<SavedServerEntity?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { savedGroups = vm.savedServers() }

    fun connectSaved(server: SavedServerEntity) {
        if (busy) return
        busy = true
        status = ""
        scope.launch {
            val ok = vm.connectSaved(server)
            if (ok) onConnected() else status = "Connection failed — server may be offline."
            busy = false
        }
    }

    fun connect() {
        if (busy || host.isBlank()) return
        busy = true
        status = ""
        scope.launch {
            val ok = vm.connect(
                host = host,
                port = port.toIntOrNull() ?: 52199,
                username = username,
                password = password,
            )
            if (ok) {
                onConnected()
            } else {
                status = "Connection failed — check host, port, and credentials."
            }
            busy = false
        }
    }

    // Scrollable + top-aligned so every field is reachable: as D-pad focus moves
    // down to Password / Connect, the scroll container brings them into view
    // (a centered, non-scrolling column clipped them under the TV overscan).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 80.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Connect to JRiver Media Center",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        // "Same server as…" target chooser: pick which real server this profile
        // belongs to. Its favorites merge into the chosen server.
        val pending = mergeProfile
        if (pending != null) {
            Column(
                modifier = Modifier.widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    "Same server as… (${pending.friendlyName ?: pending.host})",
                    style = MaterialTheme.typography.titleMedium,
                    color = JrrMuted,
                )
                savedGroups.filter { it.serverId != pending.serverId }.forEach { group ->
                    Button(
                        onClick = {
                            scope.launch {
                                vm.mergeSaved(pending.id, group.serverId)
                                mergeProfile = null
                                savedGroups = vm.savedServers()
                            }
                        },
                        colors = jrrButtonColors(),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(group.displayName) }
                }
                Button(onClick = { mergeProfile = null }, colors = jrrButtonColors()) { Text("Cancel") }
            }
        } else if (savedGroups.isNotEmpty()) {
            // Saved connections picker — tap to reconnect to a remembered server.
            Column(
                modifier = Modifier.widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Saved connections", style = MaterialTheme.typography.titleMedium, color = JrrMuted)
                savedGroups.forEach { group ->
                    if (group.profiles.size > 1) {
                        Text(
                            "${group.displayName}  (${group.profiles.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = JrrMuted,
                        )
                    }
                    group.profiles.forEach { server ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { connectSaved(server) },
                                enabled = !busy,
                                colors = jrrButtonColors(),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("${server.friendlyName ?: server.host}  —  ${server.host}:${if (server.useSsl) server.sslPort else server.port}")
                            }
                            if (savedGroups.size > 1) {
                                Button(
                                    onClick = { mergeProfile = server },
                                    enabled = !busy,
                                    colors = jrrButtonColors(),
                                ) { Text("Same as…") }
                            }
                            if (group.profiles.size > 1) {
                                Button(
                                    onClick = {
                                        scope.launch { vm.splitSaved(server.id); savedGroups = vm.savedServers() }
                                    },
                                    enabled = !busy,
                                    colors = jrrButtonColors(),
                                ) { Text("Separate") }
                            }
                            Button(
                                onClick = {
                                    scope.launch { vm.deleteSaved(server); savedGroups = vm.savedServers() }
                                },
                                enabled = !busy,
                                colors = jrrButtonColors(),
                            ) { Text("Remove") }
                        }
                    }
                }
                Text(
                    "Or add a new connection:",
                    style = MaterialTheme.typography.titleMedium,
                    color = JrrMuted,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }

        Column(modifier = Modifier.widthIn(max = 720.dp)) {
            TvTextField("Host or IP", host, { host = it }, imeAction = ImeAction.Next)
            TvTextField("Port", port, { port = it }, keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            TvTextField("Username", username, { username = it }, imeAction = ImeAction.Next)
            TvTextField(
                "Password",
                password,
                { password = it },
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { connect() },
            )
        }
        Button(
            onClick = { connect() },
            enabled = !busy && host.isNotBlank(),
            colors = jrrButtonColors(),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(if (busy) "Connecting…" else "Connect")
        }
        if (status.isNotEmpty()) {
            Text(text = status, color = JrrError, modifier = Modifier.focusable())
        }
    }
}
