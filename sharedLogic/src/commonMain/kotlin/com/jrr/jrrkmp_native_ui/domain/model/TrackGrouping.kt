package com.jrr.jrrkmp_native_ui.domain.model

/**
 * A single album within an [ArtistTrackGroup]. Keyed by [Track.albumGroupId],
 * so the separate disc folders of a multi-disc album are folded into one group.
 * [tracks] are ordered by disc then track number.
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

/**
 * Reorganises a flat track listing into an Album Artist → Album hierarchy.
 *
 * Albums are keyed by [Track.albumGroupId], which already merges the separate
 * disc folders of a multi-disc album into a single group. Within each album
 * tracks are ordered by disc then track number; artists and albums are sorted
 * case-insensitively by name.
 */
fun List<Track>.groupByArtistAndAlbum(): List<ArtistTrackGroup> =
    groupBy { it.albumArtist.ifEmpty { "Unknown Artist" } }
        .map { (artist, artistTracks) ->
            val albums = artistTracks.groupBy { it.albumGroupId }
                .map { (groupId, albumTracks) ->
                    val sorted = albumTracks.sortedWith(
                        compareBy({ it.discNumber }, { it.trackNumber }),
                    )
                    val first = sorted.firstOrNull()
                    AlbumTrackGroup(
                        groupId = groupId,
                        name = first?.album?.ifEmpty { "Unknown Album" } ?: "Unknown Album",
                        artworkFileKey = first?.fileKey ?: "",
                        tracks = sorted,
                    )
                }
                .sortedWith(compareBy { it.name.lowercase() })
            ArtistTrackGroup(artist, albums)
        }
        .sortedWith(compareBy { it.artist.lowercase() })
