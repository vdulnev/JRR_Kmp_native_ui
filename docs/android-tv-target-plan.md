# Adding an Android TV target

Status: **planned** · Branch: `feature/androidtv-target` · Author: implementation plan

This document plans a dedicated **Android TV** app for JRR, built as a new
10‑foot UI in the spirit of the existing tvOS app — not a "make the phone app
TV‑compatible" retrofit. It reuses the entire shared stack (`:sharedLogic`
ViewModels, repositories, the playback facade, Room, MCWS) and adds a fresh
D‑pad‑first UI written with **Jetpack Compose for TV**.

---

## Decisions (locked)

| Decision | Choice | Rationale |
| --- | --- | --- |
| Approach | **Option B** — dedicated module + new UI | 10‑foot UX parity with the tvOS app; no compromise between touch and remote |
| Offline mode | **None** (online‑only) | Same stance as tvOS; a TV is always near a server. Skips downloads, FileProvider, DownloadWorker |
| UI toolkit | **Jetpack Compose for TV** (`androidx.tv:tv-material`) | Purpose‑built focus/D‑pad components; the touch‑oriented `:composeUi` is not reused |
| New module | `:androidTvApp` (`com.android.application`) | Android‑only app, separate `applicationId` so it coexists with the phone app |
| Shared player | Extract `LocalPlayerHandler` + `SslHelper` into `:androidCore` library | Reused by both `:androidApp` and `:androidTvApp` without duplicating media3 wiring |
| ViewModels | Reuse the existing shared ones unchanged | `LibraryViewModel`, `NowPlayingViewModel`, `ZonesViewModel`, `SettingsViewModel`, `AlbumDetailViewModel`, `QueueViewModel` |
| Connect flow | Route through `facade.setServerConnection(...)` | Same fix we applied on tvOS: sets active server **and** flips Offline→Local so the UI comes online |

---

## Why this is straightforward (vs. tvOS)

The tvOS target was hard because it needed new Kotlin/Native klibs, SKIE
bindings, a separate SwiftUI app, and an Apple‑sandbox storage workaround.
**Android TV is just Android**:

- No new KMP target, no new framework build, no SKIE.
- `:sharedLogic` already builds for Android and exposes every ViewModel/repo.
- Room already works on Android (no `flock`/Caches issue — that was Apple‑only).
- media3 (ExoPlayer) is fully supported on Android TV.

The real work is **UI + focus**, plus a small refactor to share the Android
player engine.

---

## Current Android baseline (verified)

- `:androidApp` is a Compose Multiplatform app: `MainActivity` builds an
  `AppContainer` (DI), wires `RootComponent` (Decompose) + `MainShell`, and
  injects the shared feature ViewModels via `AppDeps` factory lambdas.
- DI: `core/di/AppContainer.kt` constructs `McwsCore.create(database)` →
  `mcwsClient` + `serverRepository`, a `LocalPlayerHandler` (media3 engine),
  and the `AudioPlayerFacade`. Prefs live in `SharedPreferences("jrr_settings")`.
- Android‑local pieces that the TV app needs but that live in `:androidApp`
  today and must be shared:
  - `playback/LocalPlayerHandler.kt` — media3 ExoPlayer engine (the Android
    `NativePlayerController`/local engine).
  - `core/network/SslHelper.kt` — trust‑all OkHttp for JRiver's self‑signed
    cert (used by Coil + media3 datasource).
- Android‑local pieces the TV app does **not** need (online‑only): `DownloadWorker`,
  `JrrFileProvider`, `VoiceSearchResolver`, the downloads directory plumbing.
- Versions: AGP `9.2.1`, Kotlin `2.3.21`, Compose Multiplatform `1.11.0`,
  media3 `1.3.1`, Decompose `3.5.0`, compileSdk `36`, minSdk `33`, targetSdk `36`.

---

## Shared ViewModels (reused as‑is)

All in `sharedLogic/.../presentation/viewmodel/` and platform‑agnostic
(`androidx.lifecycle.ViewModel`):

- `LibraryViewModel` — artists, albums‑by‑artist, random albums, browse tree,
  browse‑track grouping. (Largest VM; already drives the phone Library.)
- `AlbumDetailViewModel` — album tracks, play/queue actions.
- `NowPlayingViewModel` — transport state, artwork, progress.
- `ZonesViewModel` — device + server zones, volume, `refreshZones()`.
- `SettingsViewModel` — server info, local audio quality, log severity, disconnect.
- `QueueViewModel` — current queue (optional on TV v1).

Browse grouping reuses `data/repository/BrowseTrackGrouping.kt`
(`groupTracksByArtistAndAlbum`, `albumGroupKeyOf`) and the multi‑disc logic —
the same code the phone and tvOS browse screens use.

`LibraryRepository` surface available: `getArtists()`,
`getAlbumsByArtist(artist)`, `getRandomAlbums(limit)`, `getBrowseChildren(parentId)`,
`getBrowseFiles(nodeId)` (root `"-1"`), `getAlbumTracks(album)`, `getZones()`.

---

## Target module layout

```
:androidCore        (new, com.android.library)   ← shared Android runtime
  └─ playback/LocalPlayerHandler.kt   (moved from :androidApp)
  └─ core/network/SslHelper.kt        (moved from :androidApp)
:androidApp         (existing)        depends on :androidCore
:androidTvApp       (new, com.android.application)  depends on :sharedLogic, :androidCore
  └─ TvApplication.kt                 (Kermit bootstrap, like JrrApplication)
  └─ MainActivity.kt                  (single Activity, setContent { TvApp() })
  └─ di/TvAppContainer.kt             (online-only container)
  └─ ui/ (Compose-for-TV screens, mirrors the tvOS view set)
```

`:composeUi` is **not** a dependency of `:androidTvApp` (it's the touch UI).

### UI screen set (mirrors the tvOS app)

| tvOS Swift | Android TV Composable | Notes |
| --- | --- | --- |
| `TvRootView` | `TvRootScreen` | phases: restoring / connected / disconnected |
| `TvConnectView` | `TvConnectScreen` | host/port/user/pass form; `facade.setServerConnection` |
| `TvMainView` | `TvMainScaffold` | top `TabRow` (or `NavigationDrawer`): Playing Now / Library / Zones / Settings |
| `TvLibraryRootView` | `TvLibraryScreen` | sub‑tabs: Artists / Random Albums / Browse / Favorites |
| `TvLibraryView` (Artists) | `TvArtistsScreen` → `TvArtistAlbumsScreen` | drill‑down |
| `TvRandomAlbumsView` | `TvRandomAlbumsScreen` | grid of album cards |
| `TvBrowseView` | `TvBrowseScreen` | grouped drill‑down Artists→Albums→Tracks via `groupTracksByArtistAndAlbum` |
| `TvFavoritesView` | `TvFavoritesScreen` | favorites from `name\|albumArtist` identifiers |
| `TvAlbumTracksView` | `TvAlbumTracksScreen` | track list + play actions |
| `TvNowPlayingDetailView` | `TvNowPlayingScreen` | artwork, transport, progress |
| `TvZonesView` | `TvZonesScreen` | This Device + Server Zones, volume |
| `TvSettingsView` | `TvSettingsScreen` | quality picker, log severity, disconnect |
| `TvSearchView` | `TvSearchScreen` | text entry + results |
| `TvArtwork` | `TvArtwork` (Coil) | Coil3 `AsyncImage` with `SslHelper` OkHttp |

---

## Phases

### Phase 0 — Extract `:androidCore` (no TV yet, keeps phone app green)
- Create `:androidCore` (`com.android.library`, namespace
  `com.jrr.jrrkmp_native_ui.androidcore`), depends on `:sharedLogic` + media3 +
  OkHttp.
- Move `LocalPlayerHandler.kt` and `SslHelper.kt` from `:androidApp` into it;
  fix imports. `:androidApp` now `implementation(projects.androidCore)`.
- **Gate:** `./gradlew :androidApp:assembleDebug` builds and the phone app still
  runs. This phase ships independently of the TV work.

### Phase 1 — Module scaffold + DI
- Create `:androidTvApp` (`com.android.application`, `applicationId
  com.jrr.jrrkmp_native_ui.tv`), add `androidx.tv` + Compose BOM deps.
- `TvAppContainer`: build `McwsCore.create(db)`, `LibraryRepository(..., isOfflineProvider = AlwaysOnlineProvider)`, `LocalPlayerHandler`, `AudioPlayerFacade`
  (prefs‑backed closures, same keys as phone). `AlwaysOnlineProvider.isOffline() = false`.
- `TvApplication.onCreate` → `AppLogger.configure(...)` (Kermit, `jrr:` prefix).
- **Gate:** app installs and shows a placeholder on the ATV emulator.

### Phase 2 — Shell + navigation
- `TvMainScaffold` with Compose‑for‑TV `TabRow` for the four main tabs; remember
  selected tab. Each tab hosts its screen.
- Establish focus conventions: `Modifier.focusable`, focus restorers per tab,
  initial‑focus on entry, focused‑item scale/elevation.
- **Gate:** D‑pad moves between tabs and into content.

### Phase 3 — Connect + save/restore
- `TvConnectScreen` form. On success: `facade.setServerConnection(host, port,
  useSsl, sslPort, authToken)` then persist via `serverRepository.saveServer(...)`.
- `TvRootScreen.restore()` re‑auths from the last saved server and calls
  `facade.setServerConnection(...)` (Offline→Local), then shows the main shell.
  (Mirrors the tvOS fix so Library/Zones come online on launch.)
- **Gate:** connect, kill, relaunch → auto‑restores and library loads.

### Phase 4 — Library
- Artists list → albums‑by‑artist → album tracks (drill‑down).
- Random Albums grid.
- Browse: grouped drill‑down (Album Artists → Albums → Tracks) via
  `groupTracksByArtistAndAlbum`, plus a flat/raw browse tree fallback.
- Favorites.
- **Gate:** all four sub‑tabs populate from the live server and are D‑pad
  navigable.

### Phase 5 — Now Playing, Zones, Settings, Search
- Now Playing transport (play/pause/next/prev/seek), artwork, progress.
- Zones: This Device (Local) + server zones, select + volume.
- Settings: local audio quality picker (`LocalAudioQuality.label`), log
  severity row (debug builds), Disconnect (`container.disconnect()`).
- Search screen.
- **Gate:** can switch zones and play to each; settings/search work.

### Phase 6 — Playback wiring
- Local zone → `LocalPlayerHandler` (media3) streaming from the server
  (`checkLocalFileProvider` always returns null since there are no downloads).
- Remote zones → facade MCWS control (existing).
- Decide on a media session/foreground service: reuse a trimmed media3
  `MediaSessionService` for system transport + "now playing" on the TV, or run
  the facade in‑process for v1. (Recommend in‑process first, add session later.)
- **Gate:** gapless transport across both zone classes; no ANR.

### Phase 7 — Branding + manifest + focus polish
- TV banner (320×180, JRR monogram via `tools/icons`) → `res/drawable/banner` and
  `android:banner` on `<application>`.
- Manifest: `LEANBACK_LAUNCHER` category on `MainActivity`;
  `uses-feature android.software.leanback required="true"`;
  `uses-feature android.hardware.touchscreen required="false"`; `INTERNET` +
  cleartext (self‑signed/LAN). Leanback/TV theme.
- Focus polish pass: scroll‑into‑view, edge handling, back navigation.

### Phase 8 — Build & run
- Create an Android TV emulator (API 34, "Television (4K)" / 1080p) **or** sideload
  to a physical Android TV / Google TV device (`adb connect <ip>` then
  `adb install`).
- **Gate:** launches from the TV home row, connects, browses, plays.

---

## Dependencies to add (`gradle/libs.versions.toml`)

```toml
[versions]
androidx-tv        = "1.0.0"        # tv-material (verify latest stable at impl time)
androidx-compose-bom = "2024.09.xx" # for the androidx (non-CMP) Compose used by the TV app
coil3              = "<existing>"    # reuse phone version

[libraries]
androidx-tv-material   = { module = "androidx.tv:tv-material", version.ref = "androidx-tv" }
androidx-tv-foundation = { module = "androidx.tv:tv-foundation", version.ref = "androidx-tv" } # if needed
androidx-compose-bom   = { module = "androidx.compose:compose-bom", version.ref = "androidx-compose-bom" }
```

> Note: `:androidTvApp` uses **Google androidx Compose** (via BOM), not Compose
> Multiplatform. The shared `org.jetbrains.kotlin.plugin.compose` compiler plugin
> works for both. Because no Compose types cross the `:sharedLogic` boundary
> (it's pure logic), there is no CMP‑vs‑androidx type clash. `:composeUi` (CMP)
> stays out of this module's classpath.

---

## Risks & open questions

1. **Compose for TV maturity** — `tv-foundation` lazy lists were folded back into
   `androidx.compose.foundation`; confirm the current stable API surface
   (`TabRow`, `Card`, `ListItem`, `NavigationDrawer`, `Carousel`) at build time.
2. **Focus management** — the dominant effort. Each list/grid needs a focus
   restorer and sane initial focus; mixing scrollable content with the tab row is
   the classic ATV pitfall.
3. **media3 on TV / foreground service** — works, but the media session +
   notification model differs on TV; v1 may run the facade in‑process and defer a
   `MediaSessionService`.
4. **Self‑signed TLS** — `SslHelper` trust‑all OkHttp must be wired into both Coil3
   and the media3 datasource in `:androidCore` (already true for the phone app).
5. **Coexistence** — separate `applicationId` (`...tv`) so phone + TV installs
   don't collide; they share the same `SharedPreferences`/DB *names* but live in
   separate app sandboxes, so no data sharing (fine — both re‑auth/restore).
6. **Emulator availability** — the Android TV system image must be downloaded;
   physical Google TV sideload is the realistic test path.

---

## Verification matrix

| Check | Command / action |
| --- | --- |
| Phone app still builds after extraction | `./gradlew :androidApp:assembleDebug` |
| TV app builds | `./gradlew :androidTvApp:assembleDebug` |
| Lint/analyze | `./gradlew :androidTvApp:lintDebug` |
| Launch on emulator | install + appears on Leanback home row |
| Connect + restore | connect, relaunch, library auto‑loads |
| Zones load after login | `vm:Zones … loaded N server zones` (not "skipped (offline)") |
| Local + remote playback | play to This Device and a server zone |
| D‑pad only | full navigation with no touch/mouse |

---

## Out of scope (v1)

- Offline mode, downloads, FileProvider, DownloadWorker.
- Android Auto / CarPlay equivalents.
- Voice search integration (`VoiceSearchResolver`).
- A `MediaSessionService` with global system transport (can follow in v2).
