package com.jrr.jrrkmp_native_ui.playback.service

import android.os.Bundle
import android.provider.MediaStore
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadedTrackEntity

class VoiceSearchResolver {

    data class SearchResult(
        val tracks: List<DownloadedTrackEntity>,
        val forceShuffle: Boolean = false
    )

    fun resolve(
        query: String?,
        extras: Bundle?,
        allTracks: List<DownloadedTrackEntity>
    ): SearchResult {
        if (allTracks.isEmpty()) {
            return SearchResult(emptyList())
        }

        var cleanQuery = query?.trim()?.lowercase() ?: ""
        var forceShuffle = false

        // Detect shuffle prefixes like "shuffle", "play random", "random"
        if (cleanQuery.startsWith("shuffle ")) {
            forceShuffle = true
            cleanQuery = cleanQuery.substring("shuffle ".length).trim()
        } else if (cleanQuery.startsWith("play random ")) {
            forceShuffle = true
            cleanQuery = cleanQuery.substring("play random ".length).trim()
        } else if (cleanQuery == "shuffle" || cleanQuery == "random" || cleanQuery == "play random") {
            forceShuffle = true
            cleanQuery = ""
        }

        // 1. Check structured search extras if focus is present
        val focus = extras?.getString(MediaStore.EXTRA_MEDIA_FOCUS)
        if (!focus.isNullOrEmpty()) {
            val artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)?.trim()?.lowercase()
            val album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)?.trim()?.lowercase()
            val title = extras.getString(MediaStore.EXTRA_MEDIA_TITLE)?.trim()?.lowercase()
            val genre = extras.getString(MediaStore.EXTRA_MEDIA_GENRE)?.trim()?.lowercase()

            when (focus) {
                MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE, "vnd.android.cursor.item/genre" -> {
                    if (!genre.isNullOrEmpty()) {
                        val matched = allTracks.filter { it.genre?.lowercase()?.contains(genre) == true }
                        if (matched.isNotEmpty()) return SearchResult(matched, forceShuffle)
                    }
                }
                MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE, "vnd.android.cursor.item/artist" -> {
                    if (!artist.isNullOrEmpty()) {
                        val matched = allTracks.filter { it.artist.lowercase().contains(artist) }
                        if (matched.isNotEmpty()) {
                            // If album is also present, filter by both
                            if (!album.isNullOrEmpty()) {
                                val both = matched.filter { it.album.lowercase().contains(album) }
                                if (both.isNotEmpty()) return SearchResult(both, forceShuffle)
                            }
                            return SearchResult(matched, forceShuffle)
                        }
                    }
                }
                MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE, "vnd.android.cursor.item/album" -> {
                    if (!album.isNullOrEmpty()) {
                        val matched = allTracks.filter { it.album.lowercase().contains(album) }
                        if (matched.isNotEmpty()) {
                            if (!artist.isNullOrEmpty()) {
                                val both = matched.filter { it.artist.lowercase().contains(artist) }
                                if (both.isNotEmpty()) return SearchResult(both, forceShuffle)
                            }
                            return SearchResult(matched, forceShuffle)
                        }
                    }
                }
                MediaStore.Audio.Media.ENTRY_CONTENT_TYPE, "vnd.android.cursor.item/track" -> {
                    if (!title.isNullOrEmpty()) {
                        var matched = allTracks.filter { it.name.lowercase().contains(title) }
                        if (!artist.isNullOrEmpty()) {
                            val withArtist = matched.filter { it.artist.lowercase().contains(artist) }
                            if (withArtist.isNotEmpty()) matched = withArtist
                        }
                        if (matched.isNotEmpty()) return SearchResult(matched, forceShuffle)
                    }
                }
            }
        }

        // 2. Fallback to free text query matching
        if (cleanQuery.isEmpty()) {
            // No search query (or just "shuffle"): return all tracks and shuffle
            return SearchResult(allTracks, forceShuffle = true)
        }

        // Search for title matches
        val titleMatches = allTracks.filter { it.name.lowercase().contains(cleanQuery) }
        if (titleMatches.isNotEmpty()) {
            return SearchResult(titleMatches, forceShuffle)
        }

        // Search for artist matches
        val artistMatches = allTracks.filter { it.artist.lowercase().contains(cleanQuery) }
        if (artistMatches.isNotEmpty()) {
            return SearchResult(artistMatches, forceShuffle)
        }

        // Search for album matches
        val albumMatches = allTracks.filter { it.album.lowercase().contains(cleanQuery) }
        if (albumMatches.isNotEmpty()) {
            return SearchResult(albumMatches, forceShuffle)
        }

        // Search for genre matches
        val genreMatches = allTracks.filter { it.genre?.lowercase()?.contains(cleanQuery) == true }
        if (genreMatches.isNotEmpty()) {
            return SearchResult(genreMatches, forceShuffle)
        }

        // Broad substring match across artist + album + title
        val broadMatches = allTracks.filter {
            it.name.lowercase().contains(cleanQuery) ||
            it.artist.lowercase().contains(cleanQuery) ||
            it.album.lowercase().contains(cleanQuery)
        }
        if (broadMatches.isNotEmpty()) {
            return SearchResult(broadMatches, forceShuffle)
        }

        return SearchResult(emptyList())
    }
}
