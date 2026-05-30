package com.jrr.jrrkmp_native_ui.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Typed routes for the five top-level tabs, replacing the old magic-int
 * `MainShellState.activeTab` (`0=Library, 1=Server, 2=Player, 3=Zones,
 * 4=Settings`). Each route is [Serializable] so Essenty's `StateKeeper` can
 * persist the tab back-stack across process death.
 */
@Serializable
sealed interface RootConfig {
    @Serializable
    data object Library : RootConfig // was tab 0

    @Serializable
    data object Server : RootConfig // was tab 1

    @Serializable
    data object Player : RootConfig // was tab 2

    @Serializable
    data object Zones : RootConfig // was tab 3

    @Serializable
    data object Settings : RootConfig // was tab 4
}
