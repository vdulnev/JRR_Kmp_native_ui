package com.jrr.jrrkmp_native_ui.presentation.navigation

import com.arkivanov.decompose.ComponentContext
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.SettingsViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.ZonesViewModel

/**
 * Server tab. The `ServerManagerScreen` (Android) / connect view (iOS) take
 * `facade` + `serverRepository` directly and have no ViewModel, so this
 * component is a plain routing marker. Connection success navigates via the
 * host calling `RootComponent.onConnectSuccess()`.
 */
class ServerComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext

/** Zones tab — thin holder for the lazily-retained [ZonesViewModel]. */
class ZonesComponent(
    componentContext: ComponentContext,
    private val deps: AppDeps,
) : ComponentContext by componentContext {
    val vm: ZonesViewModel by lazy {
        retainedViewModel("zones") { deps.zonesViewModel() }
    }
}

/** Settings tab — thin holder for the lazily-retained [SettingsViewModel]. */
class SettingsComponent(
    componentContext: ComponentContext,
    private val deps: AppDeps,
) : ComponentContext by componentContext {
    val vm: SettingsViewModel by lazy {
        retainedViewModel("settings") { deps.settingsViewModel() }
    }
}
