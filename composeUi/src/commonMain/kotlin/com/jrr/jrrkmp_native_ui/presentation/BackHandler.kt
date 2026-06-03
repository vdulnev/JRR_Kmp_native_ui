package com.jrr.jrrkmp_native_ui.presentation

import androidx.compose.runtime.Composable

/**
 * Cross-platform back handler. Android delegates to the system back dispatcher;
 * desktop is a no-op (no system back gesture). Replaces the Android-only
 * `androidx.activity.compose.BackHandler` so screens can live in commonMain.
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
