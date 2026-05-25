# ViewModel Architecture Migration Plan

This document details the architectural migration plan for moving the JRiver Remote (JRR) KMP application's state management to shared Kotlin Multiplatform (KMP) ViewModels.

---

## Architectural Principles

1. **Strict View-to-ViewModel Bounds**: UI screens (both Compose and SwiftUI) must interact *only* with their corresponding screen-level ViewModel. Direct calls to repositories, facades, databases, or local player engines from the UI are forbidden.
2. **Unified Single-State Flow**: Each ViewModel must expose exactly *one* `StateFlow<ViewState>` representing the entire screen state. This ensures a clean Unidirectional Data Flow (UDF) / Model-View-Intent (MVI) pattern.
3. **No External Libraries on iOS (Option B)**: The iOS application will observe KMP `StateFlow` fields natively using Swift Concurrency (iOS 17+ `@Observable` macros and `Task` scopes), avoiding third-party dependency pollution.

---

## Phase 1: Gradle & Dependency Configuration

### 1. Version Catalog Updates
Add JetBrains Multiplatform Lifecycle ViewModel dependencies to `gradle/libs.versions.toml`:

```toml
[libraries]
# Jetpack Lifecycle ViewModel for KMP commonMain
androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", version.ref = "androidx-lifecycle" }
```

### 2. Module Build Configuration
Add the dependency to the `sharedLogic` module's `commonMain` source set inside `sharedLogic/build.gradle.kts`:

```kotlin
sourceSets {
    commonMain.dependencies {
        implementation(libs.androidx.lifecycle.viewmodel)
        // ... other dependencies
    }
}
```

---

## Phase 2: KMP ViewModels (commonMain)

Below is the design for the core ViewModels. All ViewModels inherit from `androidx.lifecycle.ViewModel` and run in the `viewModelScope`.

```
                  ┌──────────────────────────────┐
                  │       sharedLogic KMP        │
                  │                              │
                  │   ┌──────────────────────┐   │
                  │   │   LibraryViewModel   │   │
                  │   └──────────┬───────────┘   │
                  └──────────────┼───────────────┘
                                 │
                 ┌───────────────┴───────────────┐
                 │    Exported Kotlin Framework  │
                 └───────────────┬───────────────┘
                                 │
        ┌────────────────────────┴────────────────────────┐
        ▼                                                 ▼
┌──────────────────────────────┐                 ┌──────────────────────────────┐
│         Android App          │                 │           iOS App            │
│       Jetpack Compose        │                 │           SwiftUI            │
│                              │                 │                              │
│   LibraryScreen(viewModel)   │                 │   LibraryView(viewModel)     │
└──────────────────────────────┘                 └──────────────────────────────┘
```

### 1. `LibraryViewModel`
Manages search query, tab selections (Artists, Random, Browse, Favorites), and content loading.

```kotlin
package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LibraryViewState(
    val searchQuery: String = "",
    val searchResults: List<Track> = emptyList(),
    val currentTab: String = "artists",
    val artists: List<String> = emptyList(),
    val randomAlbums: List<Album> = emptyList(),
    val isOffline: Boolean = false,
    val isLoading: Boolean = false
)

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val facade: AudioPlayerFacade
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryViewState())
    val state: StateFlow<LibraryViewState> = _state.asStateFlow()

    init {
        // Observe offline status and sync tabs
        facade.activeZone
            .map { it.isOffline }
            .distinctUntilChanged()
            .onEach { isOffline ->
                _state.update { it.copy(isOffline = isOffline) }
                loadTabContent()
            }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                val results = libraryRepository.searchTracks(query)
                _state.update { it.copy(searchResults = results) }
            }
        }
    }

    fun switchTab(tab: String) {
        _state.update { it.copy(currentTab = tab) }
        loadTabContent()
    }

    fun playTrack(track: Track) {
        facade.setQueue(listOf(track), 0)
        facade.play()
    }

    private fun loadTabContent() {
        val currentState = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (currentState.currentTab) {
                "artists" -> {
                    val artistsList = libraryRepository.getArtists()
                    _state.update { it.copy(artists = artistsList, isLoading = false) }
                }
                "random" -> {
                    val albums = libraryRepository.getRandomAlbums(20)
                    _state.update { it.copy(randomAlbums = albums, isLoading = false) }
                }
                // ... other tabs
            }
        }
    }
}
```

### 2. `AlbumDetailViewModel`
Manages details of a specific album, favorites toggling, downloads, and queued tracks.

```kotlin
data class AlbumDetailViewState(
    val albumName: String,
    val artistName: String,
    val tracks: List<Track> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val downloadedTrackKeys: Set<String> = emptySet(),
    val activeDownloadJobKeys: Map<String, String> = emptyMap() // Map of FileKey to State
)

class AlbumDetailViewModel(
    private val albumName: String,
    private val artistName: String,
    private val libraryRepository: LibraryRepository,
    private val facade: AudioPlayerFacade,
    private val database: JrrDatabase
) : ViewModel() { ... }
```

### 3. `NowPlayingViewModel`
Consolidates player status, progress calculations, volume controls, shuffle/repeat modes, and technical formats.

```kotlin
data class NowPlayingViewState(
    val trackTitle: String = "Idle",
    val artistName: String = "Unknown Artist",
    val albumTitle: String = "Unknown Album",
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 0.5f,
    val isMuted: Boolean = false,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sampleRate: Int = 0,
    val activeZoneName: String = "No Zone Selected"
)

class NowPlayingViewModel(
    private val facade: AudioPlayerFacade
) : ViewModel() { ... }
```

### 4. `QueueViewModel`
Combines local queue state and remote queue fetches based on the active zone.

```kotlin
data class QueueViewState(
    val queueTracks: List<Track> = emptyList(),
    val activeIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isLocal: Boolean = true
)

class QueueViewModel(
    private val facade: AudioPlayerFacade,
    private val libraryRepository: LibraryRepository
) : ViewModel() { ... }
```

### 5. `ZonesViewModel`
Monages the list of active zones, switching zones, and their volume/mute states.

```kotlin
data class ZonesViewState(
    val zones: List<Zone> = emptyList(),
    val activeZoneId: String = "",
    val zoneStatuses: Map<String, PlayerStatus> = emptyMap()
)

class ZonesViewModel(
    private val facade: AudioPlayerFacade
) : ViewModel() { ... }
```

---

## Phase 3: Android UI Migration (Jetpack Compose)

Android UI screens will consume StateFlow from the ViewModel via `collectAsStateWithLifecycle()`:

```kotlin
// In androidApp (common Compose view)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onAlbumClick: (String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(AppColors.bg1)) {
        SearchBar(
            query = state.searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) }
        )
        
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            // Render UI purely from state fields
            when (state.currentTab) {
                "artists" -> ArtistsList(state.artists)
                "random" -> AlbumsGrid(state.randomAlbums)
            }
        }
    }
}
```

---

## Phase 4: iOS UI Migration (Option B: SwiftUI `@Observable`)

By compiling KMP ViewModels with Objective-C generics enabled, Swift code can directly wrap them in Swift 5.9's `@Observable` macro classes. This permits observing flows using native tasks without Combine boilerplates.

### 1. The `@Observable` Swift Wrapper Class
Create a Swift wrapper class inside `iosApp/Presentation` that handles mapping the KMP `StateFlow` into reactive Swift properties:

```swift
import SwiftUI
import SharedLogic

@Observable
@MainActor
class LibraryObservable {
    private let viewModel: LibraryViewModel
    
    // SwiftUI observes these properties directly
    var searchQuery: String = ""
    var searchResults: [Track] = []
    var currentTab: String = "artists"
    var artists: [String] = []
    var randomAlbums: [Album] = []
    var isOffline: Boolean = false
    var isLoading: Bool = false
    
    init(viewModel: LibraryViewModel) {
        self.viewModel = viewModel
        
        // Expose state query binding
        self.searchQuery = viewModel.state.value.searchQuery
        
        // Start streaming Kotlin StateFlow to Swift UI State
        Task {
            for await state in viewModel.state {
                self.searchQuery = state.searchQuery
                self.searchResults = state.searchResults
                self.currentTab = state.currentTab
                self.artists = state.artists
                self.randomAlbums = state.randomAlbums
                self.isOffline = state.isOffline
                self.isLoading = state.isLoading
            }
        }
    }
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
    
    func switchTab(_ tab: String) {
        viewModel.switchTab(tab: tab)
    }
    
    func playTrack(_ track: Track) {
        viewModel.playTrack(track: track)
    }
}
```

### 2. SwiftUI View Integration
The SwiftUI View becomes clean and reactive. No models, repositories, or background tasks are kept inside the view itself.

```swift
import SwiftUI
import SharedLogic

struct LibraryView: View {
    @State private var observable: LibraryObservable
    
    init(viewModel: LibraryViewModel) {
        _observable = State(initialValue: LibraryObservable(viewModel: viewModel))
    }
    
    var body: some View {
        VStack {
            TextField("Search...", text: Binding(
                get: { observable.searchQuery },
                set: { observable.updateSearchQuery($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .padding()
            
            if observable.isLoading {
                ProgressView()
            } else {
                List {
                    if observable.currentTab == "artists" {
                        ForEach(observable.artists, id: \.self) { artist in
                            Text(artist)
                        }
                    }
                }
            }
        }
    }
}
```

---

## Phase 5: Verification and Parity Plan

1. **Gradle Build Verification**: Ensure KMP multiplatform lifecycle is resolved in all targets:
   ```bash
   ./gradlew compileKotlinIosSimulatorArm64 compileDebugKotlin
   ```
2. **Unit Test Parity**: Migrated state flow mappings can be fully tested in JVM and iOS Simulator host tests by instantiating the view models and asserting state changes:
   ```bash
   ./gradlew :sharedLogic:testAndroidHostTest
   ```
3. **SwiftUI Runtime Performance**: Verify SwiftUI views update smoothly under Swift Concurrency task execution without UI hangs.
