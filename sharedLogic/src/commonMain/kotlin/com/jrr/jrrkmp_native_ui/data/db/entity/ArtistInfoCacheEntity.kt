package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * A previously fetched AI artist profile, kept so the same artist is not paid
 * for twice. An `ArtistInfoRepository` lookup costs an API call and routinely
 * takes over a minute to write a full discography, so the result is parked here
 * and served on every later view until the user explicitly refreshes.
 *
 * Keyed by artist **and** provider: OpenAI, Claude and Ollama each write a
 * different profile, so switching provider in Settings must not serve the other
 * one's answer.
 */
@Entity(
    tableName = "artist_info_cache",
    primaryKeys = ["artist_key", "provider"],
)
data class ArtistInfoCacheEntity(
    /** Lower-cased, trimmed artist name — the lookup is case-insensitive. */
    @ColumnInfo(name = "artist_key")
    val artistKey: String,
    /** Which backend produced this profile (`openai` / `claude` / `ollama`). */
    val provider: String,
    /** The artist name as displayed, before lower-casing. */
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    /** The whole [com.jrr.jrrkmp_native_ui.domain.model.ArtistInfo] as JSON. */
    @ColumnInfo(name = "info_json")
    val infoJson: String,
    /** Epoch millis the profile was fetched — shown as "cached <date>". */
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,
)
