package com.jrr.jrrkmp_native_ui.domain.model

/**
 * Audio quality used when the app asks the JRiver MC server to transcode a
 * stream (or download) on the fly via the MCWS `Conversion`/`Quality` URL
 * params. Lossless keeps the original fidelity (re-muxed to FLAC so AVPlayer /
 * ExoPlayer can decode extension-less stream URLs reliably); the lossy options
 * trade fidelity for bandwidth via Ogg Opus.
 *
 * Mirrors the canonical spec shared by the product team — keep [conversion],
 * [quality] and [label] values in sync across platforms.
 */
enum class LocalAudioQuality(
    val conversion: String,
    val quality: String,
    val label: String,
) {
    LOSSLESS("flac", "high", "Lossless"),
    LOSSY_HIGH("opus", "high", "Lossy (high)"),
    LOSSY_NORMAL("opus", "normal", "Lossy (normal)"),
    LOSSY_LOW("opus", "low", "Lossy (low)");

    /** The `Conversion=…&Quality=…` fragment appended to MCWS `GetFile` URLs. */
    val mcwsParams: String get() = "Conversion=$conversion&Quality=$quality"

    /** MIME hint matching [conversion], for out-of-band content-type on iOS. */
    val mimeHint: String get() = when (conversion) {
        "opus" -> "audio/ogg"
        else -> "audio/x-flac"
    }

    companion object {
        /**
         * Resolve a persisted enum [name] back to a value, defaulting to
         * [LOSSLESS] for null/unknown input (e.g. first launch or a value
         * written by a newer build).
         */
        fun fromName(name: String?): LocalAudioQuality =
            entries.firstOrNull { it.name == name } ?: LOSSLESS
    }
}
