package com.jrr.jrrkmp_native_ui.composeui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootComponent
import com.jrr.jrrkmp_native_ui.presentation.navigation.RootConfig

/**
 * Phase-0 toolchain spike. Renders the real shared [RootComponent] to prove the
 * Compose Desktop ↔ Decompose pipeline end-to-end: a JVM Compose window can
 * observe the shared component tree and drive its navigation.
 *
 * It only reads the active *configuration* — it never touches a child's `.vm` —
 * so stub/throwing `AppDeps` are safe (navigation components build their
 * ViewModels lazily). The real screens and DI graph arrive in Phases 1–2; this
 * composable is intentionally throwaway scaffolding.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesktopAppRoot(root: RootComponent) {
    val stack by root.stack.subscribeAsState()
    val active = stack.active.configuration

    val tabs = listOf(
        "Player" to RootConfig.Player,
        "Library" to RootConfig.Library,
        "Server" to RootConfig.Server,
        "Zones" to RootConfig.Zones,
        "Settings" to RootConfig.Settings,
    )

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "JRR Desktop — Phase 0 spike",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = "Active tab: ${active::class.simpleName}",
                    style = MaterialTheme.typography.bodyLarge,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabs.forEach { (label, config) ->
                        Button(onClick = { root.selectTab(config) }) {
                            Text(label)
                        }
                    }
                }
                Text(
                    text = "Decompose is driving navigation from :sharedLogic. " +
                        "Real screens + DI land in Phases 1–2.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
