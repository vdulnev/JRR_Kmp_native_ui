# JRR KMP Native UI

This is a Kotlin Multiplatform project designed to provide a cohesive remote control and media playback experience for JRiver Media Center across a wide range of platforms. 

The architecture is split between **Shared Business Logic** (Kotlin Multiplatform), **Shared Compose UI** (Android Mobile/Desktop), and **Native Apple/TV UI** (SwiftUI and Compose for TV).

---

## 1. What Functionality Was Implemented?
The core application serves as a full-featured remote control and media player for JRiver Media Center (MCWS) instances.
- **Library Browsing**: Browse by Artists, Albums, Compilations, and raw Folder Trees.
- **Media Playback**: Streaming playback from remote servers and local offline playback.
- **Queue Management**: Viewing, modifying, and syncing the playback queue.
- **Multi-Zone Control**: Selecting and controlling different JRiver playback zones.
- **Server Management**: Discovering, connecting to, and saving multiple servers.
- **Offline Mode**: Downloading tracks to local storage (via Room DB) for offline playback. Disconnecting from the server stops all online activity — in-flight and queued downloads are cancelled, server streaming stops, and playback falls back to the downloaded library.
- **System Integration**: Background audio playback, media session integration (Now Playing system UI), and lock-screen controls.

## 2. What Functionality is Shared?
- **Business Logic (`/sharedLogic`)**: 100% of the core logic is shared across all targets. This includes API networking (Ktor), local database (Room), downloading mechanisms, media models, and `ViewModel` state management (MVI-style flows).
- **Shared UI (`/composeUi`)**: Jetpack Compose Multiplatform UI is shared completely between **Android Mobile** and **Desktop (JVM)**. It includes all major screens (Library, Now Playing, Settings) and custom components like the Alphabet Index Bar, Vinyl Sleeve, and Vu Meter.

## 3. Target-Specific Implementations
While the business logic is shared, the UI and Audio Engines are tailored to the physical constraints of each target:
- **Android Mobile (`/androidApp`)**: Uses the shared `composeUi`. Implements playback natively via `ExoPlayer/Media3` and utilizes Android Foreground Services for background playback.
- **Desktop JVM (`/desktopApp`)**: Uses the shared `composeUi`. Implements playback via `VLCJ` to support diverse desktop audio formats natively.
- **Android TV (`/androidTvApp`)**: Uses a **completely custom UI** built with *Compose for TV*. It does not use the shared mobile `composeUi` because TV interfaces require D-Pad navigation, Leanback-style rows, and horizontal scrolling paradigms.
- **iOS Mobile (`/iosApp/iosApp`)**: Uses a **custom SwiftUI interface**. It deliberately avoids Compose UI to guarantee a 100% true Apple-native feel (utilizing native blur effects like `LiquidGlass`, navigation stacks, and custom animations). Implements playback via `AVPlayer`.
- **Apple TV / tvOS (`/iosApp/tvOSApp`)**: Uses a **custom SwiftUI interface** optimized for the Siri Remote. Utilizes a split-view `HStack` navigation paradigm tailored specifically for tvOS layout constraints.

## 4. Absent Functionality for Some Targets
- **iOS / tvOS**: Older iOS versions using `AVPlayer` may lack native FLAC/OGG decoding hardware support without software fallbacks. Offline downloading requires specialized iOS Background Task handlers which are not as deeply integrated as Android's WorkManager/Foreground Services. 
- **Desktop**: Lacks native system media key integration (e.g., Windows Media Transport Controls, MPRIS on Linux).
- **TV Targets (Android TV / tvOS)**: The Alphabet Index Bar is absent because it is unusable with a D-Pad. Offline downloading and local caching are heavily restricted or absent due to stringent TV OS storage limitations.

## 5. Recommendations for Code Sharing
To increase code reuse across the project, the following architectural shifts could be considered:
1. **Compose Multiplatform for iOS**: The biggest redundancy is the iOS SwiftUI codebase. We could migrate iOS to use `/composeUi` via Compose Multiplatform. This would unify the mobile/desktop UI codebase entirely, at the cost of losing some pixel-perfect Apple native feel and performance.
2. **Shared KMP Media Player**: Currently, every target implements its own audio engine (`ExoPlayer`, `AVPlayer`, `VLCJ`) behind the `MediaPlayerFacade`. Adopting a cross-platform Kotlin audio library could unify playback state, buffering logic, and error handling.
3. **Shared TV UI Module (`/composeTvUi`)**: If Compose Multiplatform eventually supports tvOS, we could extract the Android TV UI into a shared TV module, allowing both Android TV and Apple TV to share a single TV-optimized Compose codebase.

## 6. What Needs to be Implemented for Specific Targets
- **Desktop JVM**: Implement platform-specific media session integrations so keyboard media keys (Play/Pause/Next) control the app globally.
- **Apple TV / iOS**: Finish integrating native CarPlay UI and robust background download queue synchronization.
- **TV Targets**: Implement Voice Search and Global Search provider integrations so the OS can index the media library.
- **All Targets**: Advanced JRiver features like rating tracks, smart playlists, and dynamic tag editing.

---

## Running the Apps

- **Android Mobile**: `./gradlew :androidApp:assembleDebug`
- **Android TV**: `./gradlew :androidTvApp:assembleDebug`
- **Desktop (JVM)**: `./gradlew :desktopApp:run`
- **iOS & tvOS**: Open `/iosApp/iosApp.xcodeproj` in Xcode and run the respective scheme.