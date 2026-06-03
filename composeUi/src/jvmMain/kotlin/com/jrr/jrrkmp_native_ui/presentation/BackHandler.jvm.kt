package com.jrr.jrrkmp_native_ui.presentation

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No system back gesture on desktop; intentionally a no-op for now.
}
