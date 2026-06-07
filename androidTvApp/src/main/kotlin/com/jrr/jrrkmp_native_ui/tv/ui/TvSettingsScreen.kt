@file:OptIn(ExperimentalTvMaterial3Api::class)

package com.jrr.jrrkmp_native_ui.tv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.touchlab.kermit.Severity
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.jrr.jrrkmp_native_ui.domain.model.LocalAudioQuality
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.SettingsViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.TvConnectViewModel
import com.jrr.jrrkmp_native_ui.tv.ui.components.jrrButtonColors
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrGold
import com.jrr.jrrkmp_native_ui.tv.ui.theme.JrrMuted
import kotlinx.coroutines.launch

@Composable
fun TvSettingsScreen(
    vm: SettingsViewModel,
    connectVm: TvConnectViewModel,
    onDisconnect: () -> Unit,
) {
    val s by vm.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 48.dp, vertical = 8.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        SectionLabel("Server")
        Text(
            text = s.serverHost?.let { "$it:${s.serverPort}" } ?: "Not connected",
            color = JrrMuted,
        )

        Spacer(Modifier.height(20.dp))
        SectionLabel("Audio quality")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LocalAudioQuality.entries.forEach { quality ->
                Button(onClick = { vm.setLocalAudioQuality(quality) }, colors = jrrButtonColors()) {
                    Text(quality.label + if (quality == s.localAudioQuality) "  ✓" else "")
                }
            }
        }

        if (s.isDebugBuild) {
            Spacer(Modifier.height(20.dp))
            SectionLabel("Logging")
            
            val context = androidx.compose.ui.platform.LocalContext.current
            Button(
                onClick = {
                    val logText = vm.exportLogText()
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, logText)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "JRR debug log")
                    }
                    try {
                        context.startActivity(android.content.Intent.createChooser(intent, "Share debug log"))
                    } catch (e: Exception) {
                        android.widget.Toast.makeText(context, "No app available to share logs.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = jrrButtonColors(),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("Share log")
            }

            SectionLabel("Log level")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(Severity.Verbose, Severity.Debug, Severity.Info, Severity.Warn, Severity.Error)
                    .forEach { severity ->
                        Button(onClick = { vm.setLogSeverity(severity) }, colors = jrrButtonColors()) {
                            Text(
                                severity.name.take(1) +
                                    if (severity == s.logSeverity) " ✓" else "",
                            )
                        }
                    }
            }
        }

        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { scope.launch { connectVm.disconnect(); onDisconnect() } },
            colors = jrrButtonColors(),
        ) {
            Text("Disconnect")
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = JrrGold,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}
