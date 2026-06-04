package com.jrr.jrrkmp_native_ui.presentation

import androidx.compose.runtime.staticCompositionLocalOf
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase

/**
 * The app [JrrDatabase], provided by each host. A few screens read directly from
 * Room (e.g. LibraryScreen's favorites grid); this preserves that pre-existing
 * pattern, which previously reached the DB via Android's `context.appContainer`.
 */
val LocalDatabase = staticCompositionLocalOf<JrrDatabase> {
    error("LocalDatabase not provided — wire it in the platform host")
}
