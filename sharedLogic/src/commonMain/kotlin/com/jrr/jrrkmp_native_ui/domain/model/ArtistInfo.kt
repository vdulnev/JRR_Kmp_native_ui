package com.jrr.jrrkmp_native_ui.domain.model

import kotlinx.serialization.Serializable

/**
 * A full AI-generated artist profile: a career-length biography plus the
 * complete discography, debut to final (or latest) release.
 *
 * `@Serializable` so a fetched profile can be parked in the `artist_info_cache`
 * table as JSON (the same trick [Track] uses for the saved local queue) — an AI
 * lookup costs money and can take minutes, so it is worth not repeating.
 */
@Serializable
data class ArtistInfo(
    val artistName: String,
    /** Where the act came from, e.g. "Manchester, England". Blank when unknown. */
    val origin: String,
    /** Span of recording activity, e.g. "1976–1991" or "1994–present". Blank when unknown. */
    val activeYears: String,
    val genres: List<String>,
    /** Multi-paragraph career history — formation, each period, how it ended. */
    val biography: String,
    /** Every released album, chronological, oldest first. */
    val discography: List<DiscographyAlbum>,
) {
    /** Header line under the name: "Manchester, England · 1976–1991 · Post-punk". */
    val summaryLine: String
        get() = listOf(origin, activeYears, genres.joinToString(", "))
            .filter { it.isNotBlank() }
            .joinToString(" · ")

    /**
     * The whole profile as plain text, for the copy-to-clipboard and share
     * actions. Lives here so Compose and SwiftUI put identical text on the
     * clipboard.
     */
    fun plainText(): String = buildString {
        appendLine(artistName)
        summaryLine.takeIf { it.isNotBlank() }?.let { appendLine(it) }
        if (biography.isNotBlank()) {
            appendLine()
            appendLine(biography)
        }
        if (discography.isNotEmpty()) {
            appendLine()
            appendLine("DISCOGRAPHY (${discography.size} releases)")
            discography.forEach { album ->
                appendLine()
                append(album.year.ifBlank { "----" })
                append("  ")
                append(album.title)
                if (album.kind.isNotBlank()) append(" [${album.kind}]")
                appendLine()
                if (album.history.isNotBlank()) appendLine(album.history)
                if (album.insight.isNotBlank()) appendLine(album.insight)
            }
        }
    }.trimEnd()
}

/** One release on the [ArtistInfo.discography] timeline. */
@Serializable
data class DiscographyAlbum(
    val title: String,
    /** Year of first release as text — models sometimes qualify it ("1971", "1971 (UK)"). */
    val year: String,
    /** Studio / Live / EP / Compilation / Soundtrack. Blank when the model omits it. */
    val kind: String,
    /** How the record came about: sessions, producer, line-up, reception. */
    val history: String,
    /** One non-obvious detail about the record. */
    val insight: String,
)
