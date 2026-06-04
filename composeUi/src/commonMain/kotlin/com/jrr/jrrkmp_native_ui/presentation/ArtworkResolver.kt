package com.jrr.jrrkmp_native_ui.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Maps a remote artwork URL to a model Coil can load. A host may return a local
 * downloaded file (e.g. Android `filesDir/downloads/art_<key>.jpg`) instead of
 * the URL; the default just loads the URL as-is.
 *
 * This decouples the shared artwork components ([com.jrr.jrrkmp_native_ui.presentation.components.MiniPlayer],
 * VinylSleeve) from platform file-system access (Android `context.filesDir`,
 * desktop download dirs), which previously lived inline via `LocalContext`.
 */
fun interface ArtworkResolver {
    /** @return a Coil model — a local `File` if cached, else the original URL. */
    fun resolve(imageUrl: String): Any
}

/** Defaults to loading the URL directly (no local cache lookup). */
val LocalArtworkResolver = staticCompositionLocalOf<ArtworkResolver> {
    ArtworkResolver { it }
}
