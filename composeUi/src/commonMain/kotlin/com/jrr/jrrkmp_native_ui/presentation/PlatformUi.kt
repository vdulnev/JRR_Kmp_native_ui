package com.jrr.jrrkmp_native_ui.presentation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Host-provided platform UI actions used by the shared screens, decoupling them
 * from Android-only `Toast`/`Intent` so they can live in commonMain.
 *
 *   - Android: `Toast` + `Intent.ACTION_SEND` share sheet (provided in MainActivity).
 *   - Desktop: snackbar/log + `java.awt.Desktop`-based share (provided by the host).
 */
interface PlatformUi {
    /** Show a transient message. */
    fun showToast(message: String)

    /** Share plain text via the platform share mechanism. */
    fun shareText(text: String, subject: String? = null, chooserTitle: String? = null)
}

/** Must be provided by each host before any screen is shown. */
val LocalPlatformUi = staticCompositionLocalOf<PlatformUi> {
    error("LocalPlatformUi not provided — wire it in the platform host")
}
