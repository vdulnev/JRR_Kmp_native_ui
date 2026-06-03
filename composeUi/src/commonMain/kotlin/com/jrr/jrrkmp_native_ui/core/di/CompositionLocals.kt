package com.jrr.jrrkmp_native_ui.core.di

import androidx.compose.runtime.staticCompositionLocalOf
import com.jrr.jrrkmp_native_ui.data.api.McwsClient

/**
 * Ambient MCWS client for the composition. Provided once at the app entry
 * (see [com.jrr.jrrkmp_native_ui.MainShell]) so deeply-nested composables can
 * resolve image URLs etc. without having the client threaded through every
 * function signature.
 */
val LocalMcwsClient = staticCompositionLocalOf<McwsClient> {
    error("LocalMcwsClient not provided. Wrap your content in CompositionLocalProvider(LocalMcwsClient provides ...).")
}
