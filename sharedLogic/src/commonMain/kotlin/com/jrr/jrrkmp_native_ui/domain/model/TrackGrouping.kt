package com.jrr.jrrkmp_native_ui.domain.model

/**
 * A single album within an [ArtistTrackGroup]. [groupId] is the stable
 * multi-disc-aware album key, so the separate disc folders of a multi-disc
 * album are folded into one group. [tracks] are ordered by disc then track
 * number, with disc numbers recovered from sibling disc subfolders when the
 * per-track Disc# tags are missing.
 *
 * Produced by `groupTracksByArtistAndAlbum` (in the data layer, where the
 * shared multi-disc folding/normalisation helpers live).
 */
data class AlbumTrackGroup(
    val groupId: String,
    val name: String,
    val artworkFileKey: String,
    val tracks: List<Track>,
)

/** All albums for one album artist, used to render grouped track listings. */
data class ArtistTrackGroup(
    val artist: String,
    val albums: List<AlbumTrackGroup>,
)
