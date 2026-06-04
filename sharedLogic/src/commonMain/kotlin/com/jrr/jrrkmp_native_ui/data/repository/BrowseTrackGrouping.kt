package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.AlbumTrackGroup
import com.jrr.jrrkmp_native_ui.domain.model.ArtistTrackGroup
import com.jrr.jrrkmp_native_ui.domain.model.Track

/**
 * Reorganises a flat track listing (the Browse tab) into an Album Artist →
 * Album hierarchy, reusing the **same** multi-disc folding the Artists→Albums
 * view applies via [groupAlbumsByGroupId]:
 *
 * - albums are keyed by [computeGroupKey], which normalises disc-marker album
 *   names (`"… [CD1]"`, `"(Disc 1)"`) and folds discs that live under a shared
 *   parent folder — so a 2-CD set whose per-disc rows carry different album
 *   tags collapses into one group (the naive [Track.albumGroupId] did not);
 * - the displayed album name is the normalised base name;
 * - disc numbers are recovered from sibling disc subfolders via
 *   [Track.withFolderDiscNumber] / [assignDiscsBySubfolder] when per-track
 *   `Disc #` tags are missing, then tracks are ordered by disc then track.
 *
 * Shared by the Compose UI and (via SKIE) the iOS/macOS SwiftUI screen.
 */
fun groupTracksByArtistAndAlbum(tracks: List<Track>): List<ArtistTrackGroup> =
    tracks
        .groupBy { it.albumArtist.ifEmpty { "Unknown Artist" } }
        .map { (artist, artistTracks) ->
            val albums = artistTracks
                .groupBy { computeGroupKey(Album(it)) }
                .map { (groupId, albumTracks) ->
                    val ordered = assignDiscsBySubfolder(albumTracks.map { it.withFolderDiscNumber() })
                        .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
                    val first = ordered.firstOrNull()
                    AlbumTrackGroup(
                        groupId = groupId,
                        name = first?.let { normalizeAlbumName(it.album) }
                            ?.ifEmpty { "Unknown Album" } ?: "Unknown Album",
                        artworkFileKey = first?.fileKey ?: "",
                        tracks = ordered,
                    )
                }
                .sortedWith(compareBy { it.name.lowercase() })
            ArtistTrackGroup(artist, albums)
        }
        .sortedWith(compareBy { it.artist.lowercase() })

/**
 * Stable album-group key for a single track — matches the `groupId` of the
 * [AlbumTrackGroup] this track lands in. Used to drive collapse-all without
 * recomputing the full hierarchy.
 */
fun albumGroupKeyOf(track: Track): String = computeGroupKey(Album(track))
