package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import com.jrr.jrrkmp_native_ui.domain.model.ArtistInfo

sealed interface ArtistInfoState {
    data object Idle : ArtistInfoState
    data object Loading : ArtistInfoState
    data class Success(val info: ArtistInfo) : ArtistInfoState
    data class Error(val message: String) : ArtistInfoState
}
