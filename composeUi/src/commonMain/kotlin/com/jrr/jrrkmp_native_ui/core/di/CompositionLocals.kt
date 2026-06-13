package com.jrr.jrrkmp_native_ui.core.di

import androidx.compose.runtime.compositionLocalOf
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

/**
 * Live "played" overlay (`fileKey → latest server [Number Plays]`) sourced from
 * `AudioPlayerFacade.playCounts`. Track rows merge it with each track's baked
 * `numberPlays` so the played icon updates the moment a track finishes, without
 * a full library re-fetch. Provided at the app shell from the facade flow.
 *
 * Non-static (dynamic) so only the rows that actually read it recompose when a
 * count changes — not the whole shell subtree. Defaults to empty, which makes
 * the merge fall back to the baked count if a shell forgets to provide it.
 */
val LocalPlayCounts = compositionLocalOf<Map<String, Int>> { emptyMap() }
