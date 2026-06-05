@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.TvConnectViewModel
import com.jrr.jrrkmp_native_ui.tv.ui.components.TvTextField
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrButtonColors
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrError
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
    val scope = rememberCoroutineScope()

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
