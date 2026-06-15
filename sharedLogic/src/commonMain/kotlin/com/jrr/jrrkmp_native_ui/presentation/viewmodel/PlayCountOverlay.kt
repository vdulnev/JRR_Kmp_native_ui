package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import com.jrr.jrrkmp_native_ui.domain.model.Track

/**
 * Overlay the live server play counts (`fileKey → [Number Plays]`) from
 * [com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade.playCounts] onto a track
 * list. ViewModels fold this into their single `ViewState` so the played icon
 * reflects the authoritative server count without leaving/re-fetching the
 * screen — keeping all screen state on the one StateFlow (the UI never reads the
 * facade directly).
 *
 * Returns the same list instance when nothing changed, so an unchanged overlay
 * doesn't churn equality checks / recomposition downstream.
 */
internal fun List<Track>.overlayPlayCounts(playCounts: Map<String, Int>): List<Track> {
    if (playCounts.isEmpty() || isEmpty()) return this
    var changed = false
    val result = map { track ->
        val live = playCounts[track.fileKey]
        if (live != null && live != track.numberPlays) {
            changed = true
            track.copy(numberPlays = live)
        } else {
            track
        }
    }
    return if (changed) result else this
}
