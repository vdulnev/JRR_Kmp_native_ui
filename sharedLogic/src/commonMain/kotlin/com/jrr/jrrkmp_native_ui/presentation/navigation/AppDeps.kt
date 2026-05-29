package com.jrr.jrrkmp_native_ui.presentation.navigation

import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.AlbumDetailViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.NowPlayingViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.QueueViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.SettingsViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.ZonesViewModel

/**
 * Factory lambdas the navigation components use to build feature ViewModels.
 *
 * The component tree is intentionally decoupled from the concrete DI graph: each
 * platform host (Android `AppContainer`, iOS `AppContainer`) supplies these
 * lambdas wired to its real `AudioPlayerFacade`/repositories/database. This keeps
 * `RootComponent` and friends free of heavy constructor dependencies and makes
 * the navigation logic unit-testable with no-op factories.
 *
 * VMs are built lazily (only when a host actually renders a child), so passing
 * throwing stubs here is safe for pure navigation tests.
 */
class AppDeps(
    val libraryViewModel: () -> LibraryViewModel,
    val albumDetailViewModel: (Album) -> AlbumDetailViewModel,
    val nowPlayingViewModel: () -> NowPlayingViewModel,
    val queueViewModel: () -> QueueViewModel,
    val zonesViewModel: () -> ZonesViewModel,
    val settingsViewModel: () -> SettingsViewModel,
)
