# Adding a tvOS target

Implementation plan for shipping an **online-only Apple TV app** from this KMP
codebase. The shared business logic (MCWS networking, Decompose component tree,
ViewModels, playback facade) is reused via the `SharedLogic` framework; the UI
is a new focus-driven SwiftUI app.

> ## ⚠️ Revision (2026-06) — the persistence blocker is gone
>
> The original plan assumed Room had no tvOS klibs (true at the pinned 2.7.0)
> and prescribed a `LocalStore` abstraction + source-set refactor +
> `multiplatform-settings`. **A version check (the plan's own Phase-0 advice)
> found `androidx.room:room-runtime:2.8.4` and `androidx.sqlite:sqlite-bundled:2.6.2`
> now ship `tvos_arm64` / `tvos_simulator` / `tvos_x64` klibs.**
>
> So the implemented path is much smaller — **Phases 1–2 (LocalStore /
> source-set surgery) are dropped entirely**:
> 1. Upgrade Room 2.7.0 → 2.8.4, `androidx.sqlite` 2.5.0 → 2.6.2; verify
>    existing targets still build.
> 2. Add `tvosArm64` + `tvosSimulatorArm64` to `sharedLogic` (framework export +
>    Room `kspTvos*`). All `appleMain` actuals (`DatabaseBuilder.apple`,
>    `PlatformHttpClient.apple`, `IosLocalPlayerEngine`) are inherited by tvOS.
> 3. Link the `SharedLogic` framework for tvOS.
> 4. New tvOS Xcode app + focus-driven SwiftUI UI; downloads hidden (online-only).
>
> The sections below are kept for historical context; the `LocalStore` design is
> **not** being implemented.

## Decisions (locked)

| Decision | Choice |
| --- | --- |
| Feature scope | **Online-only** — no offline downloads / download manager on tvOS |
| Persistence (tvOS) | **multiplatform-settings** (`NSUserDefaults`-backed) behind a `LocalStore` interface; **no SQLite/Room** on tvOS |
| Persistence (other targets) | **Keep Room** — `RoomLocalStore` implements the same interface; phone/desktop behaviour unchanged |
| Architectures | `tvosArm64` (device) + `tvosSimulatorArm64` (Apple-Silicon simulator). No `tvosX64`. |
| Minimum tvOS | **tvOS 17.0** (adjustable) |
| UI strategy | **New SwiftUI tvOS app** (focus engine, 10-foot layout). No Compose (composeUi targets Android/JVM only). Reuse shared VMs/components, not views. |
| Local playback | `AVPlayer`-based engine in a tvOS `actual` (mirrors the iOS `CorePlayer`) |

## Why this is needed (verified findings)

Module-metadata inspection of the pinned versions:

- `androidx.room:room-runtime:2.7.0` native targets → `ios_*`, `macos_*`,
  `linux_*`. **No `tvos_*`.**
- `androidx.sqlite:sqlite-bundled:2.5.0` → same. **No `tvos_*`.**
- `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.11.0-beta01` →
  **has** `tvos_arm64` + `tvos_simulator` ✅ (not a blocker).
- Ktor 3.0.0 (Darwin), coroutines 1.10.1, serialization 1.8.0, Kermit 2.1.0,
  Decompose/Essenty 3.5.0, atomicfu, SKIE 0.10.12 → all ship tvOS targets ✅.

Room/SQLite are declared in **`commonMain`** and consumed across
`ServerRepository`, `LibraryRepository`, `McwsCore`, `AudioPlayerFacade`,
`QueueViewModel`, `SettingsViewModel`, `AppDeps` — so they are forced onto
every target. Dropping the *download feature* removes UI, not the dependency.
The DB also backs **saved servers, favorites, and the local queue**, which the
tvOS app still wants. Hence the `LocalStore` abstraction.

## Current persistence surface (baseline)

`JrrDatabase` (`data/db/`) exposes 6 DAOs over 6 entities:

| Entity / DAO | Used by | tvOS needs it? |
| --- | --- | --- |
| `SavedServerEntity` / `SavedServerDao` | `ServerRepository` | **yes** — connect to a server |
| `FavoriteEntity` / `FavoriteDao` | `LibraryRepository`, Compose `FavoritesTab` (via `LocalDatabase`), iOS `stateObserver` | yes |
| `LocalQueueStateEntity` / `LocalQueueStateDao` | `AudioPlayerFacade` / `QueueViewModel` | optional (could be in-memory) |
| `LocalQueueTrackEntity` / `LocalQueueTrackDao` | same — `track_json` already stores a serialized `Track` | optional |
| `DownloadedTrackEntity` / `DownloadedTrackDao` | downloads | **no** (excluded) |
| `DownloadJobEntity` / `DownloadJobDao` | downloads | **no** (excluded) |

Builder seam already exists: `expect class DatabaseBuilder` (commonMain) with
`android` / `jvm` / `apple` actuals, plus `createDatabase(builder)`.

Wrinkle to note: the Compose `FavoritesTab` touches `favoriteDao()` directly via
the `LocalDatabase` composition local. That stays valid on Android/JVM (they keep
Room) but ideally routes through `LocalStore` too (Phase 1.4).

---

## Target architecture: `LocalStore` abstraction

Introduce a persistence interface in `commonMain`; keep Room behind it for the
existing platforms, add a settings-backed impl for tvOS.

```kotlin
// commonMain — domain models (plain @Serializable, no Room annotations)
@Serializable data class SavedServer(
    val id: String, val host: String, val port: Int = 52199,
    val username: String, val passwordKey: String,
    val friendlyName: String? = null, val lastUsedAt: Long = 0L,
    val authToken: String? = null, val useSsl: Boolean = false, val sslPort: Int = 52200,
)
@Serializable data class Favorite(
    val type: String, val identifier: String, val displayName: String, val addedAt: Long,
)
@Serializable data class QueueSnapshot(val currentIndex: Int, val tracks: List<Track>)

// commonMain — the seam every repository depends on
interface LocalStore {
    // servers
    suspend fun getAllServers(): List<SavedServer>
    suspend fun getLastUsedServer(): SavedServer?
    suspend fun upsertServer(server: SavedServer)
    suspend fun deleteServer(id: String)
    // favorites
    suspend fun getAllFavorites(): List<Favorite>
    fun favoritesFlow(): Flow<List<Favorite>>
    suspend fun upsertFavorite(fav: Favorite)
    suspend fun deleteFavorite(type: String, identifier: String)
    // local queue (per zone)
    suspend fun loadQueue(zoneId: String): QueueSnapshot?
    suspend fun saveQueue(zoneId: String, snapshot: QueueSnapshot)
    suspend fun clearQueue(zoneId: String)
    // downloads are intentionally NOT in this interface — they stay
    // Room-only and are gated out of the tvOS build/UI.
}
```

Two implementations:

- **`RoomLocalStore`** (existing platforms) — wraps the current DAOs, mapping
  `SavedServer ↔ SavedServerEntity`, etc. Behaviour identical to today.
- **`SettingsLocalStore`** (tvOS) — `multiplatform-settings` over
  `NSUserDefaults`; servers/favorites stored as a serialized JSON list under a
  key, queue stored as a `QueueSnapshot` JSON per zone. `favoritesFlow()` backed
  by a `MutableStateFlow` re-emitted on writes (NSUserDefaults has no native
  observation we need here).

Downloads (`DownloadedTrack*`, `DownloadJob*`, `LibraryRepository.startDownload`,
`DownloadWorker`, the Downloads tab) remain Room-backed and are **excluded from
the tvOS source set and UI** — not abstracted.

### Source-set restructuring

Room must leave `commonMain` so tvOS never references it. Add a manual
intermediate set that every Room platform shares, excluding tvOS:

```
commonMain                      // LocalStore, models, repos, VMs, MCWS — NO Room
├── roomMain  (dependsOn common) // JrrDatabase, DAOs, entities, RoomLocalStore,
│   │                            //   expect DatabaseBuilder, createDatabase()
│   ├── androidMain              // DatabaseBuilder.android, Room ksp
│   ├── jvmMain                  // DatabaseBuilder.jvm, Room ksp
│   └── appleRoomMain (dependsOn roomMain + appleMain)
│       ├── iosMain              // DatabaseBuilder.apple (moved here)
│       └── macosMain
├── appleMain (dependsOn common) // Ktor Darwin client, AVPlayer engine — shared
│   │                            //   by ios/macos/tvos
│   ├── (iosMain, macosMain also dependsOn appleRoomMain above)
│   └── tvosMain (dependsOn appleMain)  // SettingsLocalStore, tvOS player actual
```

Mechanics in `sharedLogic/build.gradle.kts`:
- Keep `applyDefaultHierarchy()` (gives `appleMain` over `ios*/macos*/tvos*`).
- Create `roomMain` and `appleRoomMain` via `sourceSets.create(...)` with
  explicit `dependsOn`; wire `androidMain`/`jvmMain`/`iosMain`/`macosMain` into
  the Room lineage; leave `tvosMain` out of it.
- Move Room `implementation`/`api` deps and `ksp*` entries from `commonMain` to
  `roomMain` (+ `kspTvos*` is **never** added).
- Add `tvosArm64()` / `tvosSimulatorArm64()` to the framework target list with
  the same `export(...)` block.

This is the riskiest mechanical step; Phase 0 de-risks it.

---

## Phases

### Phase 0 — Spike (throwaway branch, ~½ day)

Confirm the blocker list before committing to the refactor.
1. Add `tvosArm64()`/`tvosSimulatorArm64()` to `sharedLogic` targets.
2. `./gradlew :sharedLogic:compileKotlinTvosSimulatorArm64`.
3. Record every unresolved symbol. **Expectation:** only Room/SQLite (and
   anything transitively using them). Anything else surfaces here.
4. Discard the branch; feed findings into Phase 1 scope.

### Phase 1 — `LocalStore` abstraction (no tvOS yet, fully testable today)

1.1 Add `LocalStore` interface + `SavedServer`/`Favorite`/`QueueSnapshot`
    models to `commonMain`.
1.2 Implement `RoomLocalStore` (wraps current DAOs + entity↔model mappers).
1.3 Refactor consumers to depend on `LocalStore` instead of DAOs:
    `ServerRepository`, `LibraryRepository` (favorites + queue paths only),
    `AudioPlayerFacade`, `QueueViewModel`, `McwsCore`, `AppDeps`, `SettingsViewModel`.
1.4 Route Compose `FavoritesTab` and iOS favorites through `LocalStore`
    (replace direct `favoriteDao()` use / provide a `LocalStore` composition
    local + Swift accessor).
1.5 Wire `RoomLocalStore` in all three `AppContainer`s (Android/iOS/desktop) and
    `MainActivity`/`Main.kt`/desktop DI.
1.6 **Verify**: `flutter`-equivalent gates — `./gradlew :sharedLogic:jvmTest`,
    Android + desktop compile, iOS + macApp build. Behaviour unchanged; Room
    still in `commonMain` at this point.

### Phase 2 — Move Room out of `commonMain`

2.1 Create `roomMain` + `appleRoomMain` source sets; move `JrrDatabase`, DAOs,
    entities, `RoomLocalStore`, `DatabaseBuilder` (expect + actuals),
    `createDatabase`, `LoggingSQLiteDriver` into them.
2.2 Move Room/`androidx.sqlite` deps and `ksp*` config out of `commonMain`.
2.3 `commonMain` now compiles with no Room reference.
2.4 **Verify**: all existing targets (android, jvm, iosArm64, iosSimulatorArm64,
    macosArm64) still build + tests pass. No tvOS yet → pure structural change.

### Phase 3 — Add tvOS to `sharedLogic`

3.1 Add `tvosArm64()`/`tvosSimulatorArm64()` targets + framework `export`.
3.2 Add `multiplatform-settings` deps to a `tvosMain` (or `appleMain` if also
    desired elsewhere) source set; add the lib to `libs.versions.toml`.
3.3 Implement `SettingsLocalStore` (tvOS) over `NSUserDefaultsSettings` +
    kotlinx.serialization.
3.4 Provide tvOS `actual`s for any `expect` reachable from tvOS:
    - player engine — `AVPlayer`-based `actual` (reuse the iOS state-holder
      pattern; real audio via a Swift `CorePlayer` for tvOS).
    - `PlatformHttpClient` — Ktor Darwin (already in `appleMain`, portable).
3.5 **Verify**: `./gradlew :sharedLogic:linkDebugFrameworkTvosSimulatorArm64`
    produces a `SharedLogic.framework`.

### Phase 4 — tvOS Xcode app

4.1 New `tvOSApp` target in `iosApp.xcodeproj` (or a sibling project) with a
    "Compile Kotlin Framework" build phase running
    `embedAndSignAppleFrameworkForXcode` for the tvOS SDK.
4.2 `AppContainer` (tvOS) wires `SettingsLocalStore` (no `JrrDatabase`), the
    MCWS stack, and the `AVPlayer` engine.
4.3 Build the focus-driven SwiftUI UI:
    - Server connect / picker (reads `LocalStore` servers).
    - Browse (folders → grouped tracks), Search, Artists/Albums.
    - Now Playing + zone control (this app's strength on the TV).
    - Favorites.
    - **Omit**: Downloads tab, offline mode, "share debug log" intent (or keep a
      read-only log view).
4.4 Reuse shared Decompose components/ViewModels through the framework; only
    views are new.
4.5 **Verify**: `xcodebuild -scheme tvOSApp -sdk appletvsimulator build`.

### Phase 5 — Polish

- tvOS app icon (layered `.imagestack` / single-layer per current minimums) +
  Top Shelf image. Extend `tools/icons/genicons.swift` to emit the tvOS sizes.
- Focus styling (hover/parallax), large-text legibility, remote-play gestures.
- Now-Playing info / remote-command center on tvOS.

---

## Dependencies to add

`gradle/libs.versions.toml`:

```toml
multiplatform-settings = "1.2.0"
multiplatform-settings-core = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatform-settings" }
multiplatform-settings-serialization = { module = "com.russhwolf:multiplatform-settings-serialization", version.ref = "multiplatform-settings" }
```

(verify the artifact ships `tvos_arm64`/`tvos_simulator` klibs when wiring —
multiplatform-settings covers the full Apple target set, but pin-check as for Room.)

## Feature scope on tvOS

| Included | Excluded |
| --- | --- |
| Connect to saved servers; server picker | Offline downloads / download manager |
| Browse, search, artists/albums, grouped tracks | `DownloadWorker`, on-device track cache |
| Play to remote zones + local `AVPlayer` playback | "Designed for iPad" fallback |
| Now Playing, queue, favorites | Per-track local file storage |

## Risks & open questions

- **Source-set surgery (Phase 2)** is the highest-risk change; it touches every
  platform. Phase 1 keeps it behavior-preserving so Phase 2 is purely structural
  and independently verifiable.
- **If a future Room ships tvOS klibs**, Phases 2–3 collapse: keep Room in
  `commonMain`, add tvOS targets, and tvOS just hides the download UI. Worth a
  version check before starting Phase 2 — it could save the whole refactor.
- **Queue serialization**: `LocalQueueTrackEntity.track_json` already serializes
  `Track`; `QueueSnapshot` reuses the same `Track` serializer, so no new format.
- **NSUserDefaults size**: servers/favorites/queue are tiny; well within limits.
  (If the queue ever grows large, swap the tvOS queue store to `kstore`/a file.)
- **SKIE tvOS**: confirm the exported Decompose/Essenty surface generates cleanly
  for the tvOS framework (it supports tvOS, but validate during Phase 3).
- **AVPlayer on tvOS**: confirm background/now-playing-info behaviour and remote
  command handling differ acceptably from iOS.

## Verification matrix

| Phase | Gate |
| --- | --- |
| 0 | `compileKotlinTvosSimulatorArm64` blocker list captured |
| 1 | jvmTest green; Android + desktop compile; iOS + macApp build; behaviour unchanged |
| 2 | all existing targets build + tests pass (no tvOS) |
| 3 | `linkDebugFrameworkTvosSimulatorArm64` succeeds |
| 4 | `xcodebuild -sdk appletvsimulator` succeeds; connect + browse + play on the tvOS simulator |
