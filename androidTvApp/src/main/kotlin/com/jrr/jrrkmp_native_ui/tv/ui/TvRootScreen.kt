@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.tv.di.TvAppContainer

private enum class Phase { Restoring, Connected, Disconnected }

/**
 * App entry point. On launch the [TvConnectViewModel] restores the last-used
 * server; the connect form shows only when there's nothing to restore or it
 * fails. All connection logic lives in the ViewModel — this is pure navigation.
 */
@Composable
fun TvRootScreen(container: TvAppContainer) {
    var phase by remember { mutableStateOf(Phase.Restoring) }
    val connectVm = remember { container.makeTvConnectViewModel() }

    when (phase) {
        Phase.Restoring -> {
            LaunchedEffect(Unit) {
                phase = if (connectVm.restore()) Phase.Connected else Phase.Disconnected
            }
            Loading()
        }
        Phase.Connected -> TvMainScaffold(
            container = container,
            onDisconnect = { phase = Phase.Disconnected },
        )
        Phase.Disconnected -> TvConnectScreen(
            vm = connectVm,
            onConnected = { phase = Phase.Connected },
        )
    }
}

@Composable
private fun Loading() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connecting…")
        }
    }
}
