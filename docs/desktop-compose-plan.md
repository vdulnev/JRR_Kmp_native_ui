# Windows Desktop (Compose Multiplatform) Implementation Plan

> **Status:** 📋 **Proposed — for review.** This plan targets a native-feeling
> Windows desktop build using **Compose Multiplatform for Desktop** (JVM/Skiko),
> reusing the existing Decompose component tree and Compose UI. It builds on the
> in-progress groundwork on `feature/add-mingwX64-target` (rebased on `main`),
> where the shared module's desktop target was switched from `mingwX64()`
> (Kotlin/Native — no Room/Decompose/Essenty artifacts exist for Windows-native)
> to **`jvm()`**. See [windows-target rationale](#appendix-a-why-jvm-not-mingwx64).
>
> **Relationship to the other platforms:** `main` now also ships a native
> **macOS** target (`macosArm64`, sharing the `appleMain` source set with iOS —
> see [macos-target-plan.md](macos-target-plan.md)). Apple platforms (iOS +
> macOS) deliberately use **native SwiftUI**; Android and this Windows desktop
> build are the two hosts that reuse the **Compose** UI. That split is what makes
> extracting a shared `:composeUi` module (§3) worthwhile — it is consumed by
> `:androidApp` and `:desktopApp` only. Every claim below is grounded in the
> cited files.

---

## 1. Goal & scope

### Goal

A runnable Windows desktop app (`.exe`/`.msi`) that reuses the shared business
logic, the Decompose navigation tree, and as much of the existing Compose UI as
practical — rendered by Compose Multiplatform Desktop, not native Win32/WinUI
widgets.

### Why this is mostly viable today

The UI is already written against **JetBrains Compose Multiplatform artifacts**,
not Android-only Compose:

- [androidApp/build.gradle.kts:5](../androidApp/build.gradle.kts) applies
  `org.jetbrains.compose` (`composeMultiplatform = "1.11.0"`,
  [libs.versions.toml:12](../gradle/libs.versions.toml)).
- The Compose deps resolve to `org.jetbrains.compose.*` modules — `compose-ui`,
  `compose-foundation`, `compose-material3`
  ([libs.versions.toml:44-48](../gradle/libs.versions.toml)).
- `compose-components-resources` is **already declared**
  ([libs.versions.toml:48](../gradle/libs.versions.toml)), so the Compose
  Resources pipeline (fonts/images/strings) is available with no new dependency.
- Navigation is UI-agnostic Decompose in `commonMain`
  (`RootComponent`, `LibraryComponent`, `PlayerComponent`,
  [RootComponent.kt](../sharedLogic/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/navigation/RootComponent.kt));
  the Android host binds to it via `extensions-compose` (`subscribeAsState`,
  `Children`) exactly as desktop will.
- ViewModels, repositories, the playback facade, and the DB/HTTP `expect/actual`
  seams all live in `sharedLogic/commonMain` and now compile for `jvm()`
  (verified: `:sharedLogic:compileKotlinJvm` BUILD SUCCESSFUL).

So the heavy lifting is **not** rewriting screens — it is (a) relocating the
shared UI out of the `androidApp` *application* module so a second host can
depend on it, and (b) supplying desktop equivalents for the handful of
Android-only couplings.

### What this plan delivers

1. A new **`composeUi` Kotlin Multiplatform library module** holding the
   platform-agnostic Compose UI (screens, components, theme), with `androidMain`
   + `desktopMain` (JVM) source sets.
2. A new **`desktopApp` JVM application module** with a `main()` that opens a
   Compose `Window`, builds a desktop `AppContainer`, and renders the shared
   `RootComponent`.
3. Desktop `actual`/implementations for the Android-coupled pieces
   (player engine, settings store, image loading, fonts).
4. Packaging config to produce a Windows `.msi`/`.exe`.

---

## 2. Current-state inventory: what's reusable vs. Android-coupled

The Compose UI currently lives **inside the `androidApp` application module**
(`androidApp/src/main/kotlin/.../presentation/`,
`.../core/theme/`). An application module cannot be a dependency of another
module, so this code must move to a library module to be shared.

### Reusable as-is (pure Compose Multiplatform)

- All screens: `LibraryScreen`, `LibraryLargeScreen`, `AlbumDetailScreen`,
  `NowPlayingScreen`, `QueueScreen`, `ZonesScreen`, `SettingsScreen`,
  `ServerManagerScreen`
  (`androidApp/src/main/kotlin/.../presentation/screens/`).
- Components: `MiniPlayer`, `VuMeter`, `VinylSleeve`, `AlphabetIndexBar`,
  `InfoDialog` (`.../presentation/components/`).
- Shell: `LargeScreenShell` (`.../presentation/shell/`).
- Theme: `JrrTheme` ([JrrTheme.kt](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/core/theme/JrrTheme.kt))
  is pure Compose (Material3 + CompositionLocals) — moves unchanged. Same for
  `AppColors`, `AppSpacing`, `AppTypography` (pending the font note below).

### Android-coupled — needs a desktop path

| Coupling | Where | Desktop resolution |
| --- | --- | --- |
| **Fonts via `R.font.*`** | [AppFonts.kt:6-19](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/core/theme/AppFonts.kt) | Move `.ttf` into `composeUi/src/commonMain/composeResources/font/` and load via generated `Res.font.*` (Compose Resources). Works on both platforms. |
| **`Toast`** | [MainActivity.kt:185](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/MainActivity.kt) | Hoist toast to a shared `SnackbarHost` or a small `expect fun showToast`. Toast is host glue, not in the screens. |
| **`LocalConfiguration.current.screenWidthDp`** | [MainActivity.kt:208](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/MainActivity.kt) | Desktop derives width from window size (`BoxWithConstraints` at the root). Desktop is always ≥840dp → uses the existing `LargeScreenShell` path. |
| **Image loading: Coil 2 (Android-only)** | [JrrApplication.kt:28-39](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/JrrApplication.kt), `coil = "2.6.0"` ([libs.versions.toml:23](../gradle/libs.versions.toml)) | Migrate to **Coil 3** (`io.coil-kt.coil3`, multiplatform) with `coil-network-ktor` reusing the shared Ktor client — incl. the trust-all SSL config already needed for self-signed JRiver servers. See §5 Decision C. |
| **`material-icons-extended` (androidx 1.7.0)** | [libs.versions.toml:66](../gradle/libs.versions.toml), used in [MainActivity.kt:11-13](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/MainActivity.kt) | Switch the shared module to the JetBrains MP icons artifact, or vendor the ~4 icons actually used (`Home`, `List`, `PlayArrow`, `Settings`). See §5 Decision D. |
| **DI host: `AppContainer(Context)`** | [AppContainer.kt:29](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/core/di/AppContainer.kt) | Desktop gets its own `AppContainer` (no Context) building the same graph: `createDatabase` (jvm `DatabaseBuilder` ✓), `McwsCore`, repos, facade. |
| **Player: `LocalPlayerHandler` (Media3/ExoPlayer)** | `androidApp/.../playback/LocalPlayerHandler.kt`, `media3 = "1.3.1"` | Desktop `LocalPlayerEngine` impl backed by **VLCJ/libvlc** (decided — §5 Decision A). |
| **Settings via `SharedPreferences`** | [MainActivity.kt:73-79](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/MainActivity.kt), [AppContainer.kt:50-52](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/core/di/AppContainer.kt) | Desktop `MainShellSettings` + prefs backed by `java.util.prefs.Preferences` or a JSON file under `%LOCALAPPDATA%` (mirrors the DB path in [DatabaseBuilder.jvm.kt](../sharedLogic/src/jvmMain/kotlin/com/jrr/jrrkmp_native_ui/data/db/DatabaseBuilder.jvm.kt)). |
| **`DownloadWorker` / WorkManager** | [AppContainer.kt:99-107](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/core/di/AppContainer.kt) | Desktop downloads run on a coroutine + Ktor instead of WorkManager. Defer if downloads are out of scope for v1. |

### The two genuinely new pieces of work

1. **Desktop audio playback** — the `LocalPlayerEngine` interface has ~25 methods
   ([LocalPlayerEngine.kt](../sharedLogic/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/playback/LocalPlayerEngine.kt)).
   Android uses Media3; iOS/macOS bridge to a Swift `CorePlayer` (the
   `IosLocalPlayerEngine` state holder in `appleMain`). Desktop has no built-in
   player.
2. **A desktop image loader** (Coil 3 migration) to replace the Android Coil 2
   `ImageLoaderFactory`.

---

## 3. Target module structure

```
:sharedLogic        (existing) — commonMain logic; targets: androidLibrary,
                      iosArm64, iosSimulatorArm64, macosArm64, and now jvm() ✓
                      Apple actuals live in src/appleMain (shared iOS + macOS);
                      desktop actuals live in src/jvmMain (DatabaseBuilder.jvm.kt,
                      PlatformHttpClient.jvm.kt).
:composeUi          (NEW, KMP library)
    commonMain      — screens, components, theme, shell, JrrTheme, AppDeps-bound UI
                      composeResources/font/*.ttf  (moved from androidApp res/font)
    androidMain     — Android-only UI glue (Toast impl, Coil Android tweaks if any)
    desktopMain     — desktop-only UI glue (window-size adaptivity helpers)
    depends on: :sharedLogic, compose.* , decompose-extensions-compose, coil3
    NOTE: Apple targets are NOT added here — iOS/macOS keep native SwiftUI.
:androidApp         (existing) — thinned: Application, MainActivity, AppContainer,
                      Media3 LocalPlayerHandler, WorkManager, FileProvider.
                      Now depends on :composeUi instead of holding the UI.
:desktopApp         (NEW, JVM application)
    main() → application { Window { JrrTheme { App(root) } } }
    DesktopAppContainer, DesktopMainShellSettings, VLCJ LocalPlayerEngine,
    AppLogger.configure(), Coil3 setup.
    depends on: :composeUi, :sharedLogic
iosApp / macApp     (existing, native SwiftUI — out of scope here; see
                      macos-target-plan.md). Consume sharedLogic's Apple framework.
```

Rationale for a separate `:composeUi` module (vs. folding UI into `sharedLogic`):
keeps the Compose Multiplatform plugin and its compiler off the pure-logic module
(faster Apple framework builds, cleaner `commonTest`), and mirrors the existing
split where `sharedLogic` is deliberately UI-agnostic. The module is consumed by
the two Compose hosts only — `:androidApp` and `:desktopApp` — while iOS/macOS
bind to the same Decompose tree through native SwiftUI.

---

## 4. Phased implementation

Staged so a runnable window appears early and the big UI move is de-risked by a
thin vertical slice first.

### Phase 0 — Toolchain spike (thin vertical slice) ✅ DONE

Goal: prove the Compose Desktop toolchain end-to-end before moving real UI.

- Add `:desktopApp` JVM module + `:composeUi` skeleton (commonMain with **one**
  trivial composable).
- Wire `desktopApp` `main()`: `application { Window(onCloseRequest = ::exitApplication) { … } }`.
- Render the real `RootComponent` driven by a `LifecycleRegistry` +
  `DefaultComponentContext`, with **stub** `AppDeps` factories (throwing lambdas
  are safe — VMs build lazily, per
  [AppDeps.kt:22-23](../sharedLogic/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/navigation/AppDeps.kt)).
- **Exit criteria:** `./gradlew :desktopApp:run` opens a window showing the slice.

**Outcome (implemented):**
- `:composeUi` ([composeUi/build.gradle.kts](../composeUi/build.gradle.kts)) — KMP
  library, **`jvm()` target only** for now (androidLibrary target lands in
  Phase 1); holds [DesktopAppRoot.kt](../composeUi/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/composeui/DesktopAppRoot.kt),
  which `subscribeAsState()`s the real `RootComponent.stack` and renders tab
  buttons calling `root.selectTab(...)`.
- `:desktopApp` ([desktopApp/build.gradle.kts](../desktopApp/build.gradle.kts) +
  [Main.kt](../desktopApp/src/jvmMain/kotlin/com/jrr/jrrkmp_native_ui/desktop/Main.kt)) —
  Compose Desktop application; `compose.desktop.application` with Msi/Exe formats
  pre-declared for Phase 5.
- Both registered in [settings.gradle.kts](../settings.gradle.kts).
- Verified: `:desktopApp:compileKotlinJvm` builds; `:desktopApp:run` opens the
  window and logs `Debug: (vm:RootNav) init initialConfig=Player` with no errors.

**Tooling requirement (Compose Hot Reload):** CMP 1.11 bundles Compose Hot
Reload, whose run tasks (`:desktopApp:hotRunJvm`, `reload`, and the IDE's
Compose "Run" gutter) launch on a **JetBrains Runtime (JBR) 21** with
`-XX:+AllowEnhancedClassRedefinition`. With no JBR registered and no toolchain
download resolver, they fail with *"Failed to find suitable JetBrains Runtime 21
installation."* Fixed two ways: (1) the **foojay resolver** in
[settings.gradle.kts](../settings.gradle.kts) auto-downloads a JBR on any
machine/CI that lacks one; (2) machines with an IDE-bundled JBR (Android Studio /
IntelliJ) can register it via `org.gradle.java.installations.paths` in the
**user-global** `~/.gradle/gradle.properties` to skip the download. The plain
`:desktopApp:run` task does **not** need a JBR (it uses the regular JDK).

**Gotcha recorded for later phases:** Decompose's "main thread" on desktop is the
**AWT event-dispatch thread (EDT)**, not the JVM `main` thread (which is *named*
"main" but isn't the EDT). Constructing the component tree on the JVM main thread
throws `NotOnMainThreadException`. The fix: build the root during composition
inside `application { remember { … } }` (Compose Desktop composes on the EDT).
The Phase 2 window host must keep creating/resuming the component tree on the EDT.

### Phase 1 — Extract shared UI into `:composeUi` ✅ COMPLETE

The UI is **not** a pure move — the 20 files carry Android couplings that must
gain a common/desktop path first: **Coil 2** (6 files), **`android.widget.Toast`
/ `Intent`** (8 files), **`R.font`** (AppFonts), and **androidx `material-icons`**
(most screens). So Phase 1 runs as verifiable increments, each gated on
`:androidApp:assembleDebug` still building unchanged.

**Increment 1 — two-target module + font-independent primitives ✅ DONE**
- Added the **`androidLibrary` target** to `:composeUi` alongside `jvm()`
  ([composeUi/build.gradle.kts](../composeUi/build.gradle.kts)) — validated that
  Compose compiles for the new AGP KMP android-library target *and* desktop.
- Moved the coupling-free primitives to `composeUi/commonMain`, identical
  packages (zero consumer-import changes): `AppColors` (354 refs), `AppSpacing`,
  `UiHelpers`, `VuMeter`.
- Repointed `:androidApp` to `implementation(projects.composeUi)`.
- Verified: `:androidApp:assembleDebug` + `:desktopApp:compileKotlinJvm` both green.

**Increment 2 — fonts + rest of `core.theme` ✅ DONE**
- Kept `AppFonts`/`AppTypography` as **static objects**; supplied the
  `FontFamily`s via `expect`/`actual` — androidMain
  ([AppFonts.android.kt](../composeUi/src/androidMain/kotlin/com/jrr/jrrkmp_native_ui/core/theme/AppFonts.android.kt))
  uses `R.font` (fonts copied to `composeUi/src/androidMain/res/font`, with
  `androidResources` enabled); jvmMain
  ([AppFonts.jvm.kt](../composeUi/src/jvmMain/kotlin/com/jrr/jrrkmp_native_ui/core/theme/AppFonts.jvm.kt))
  uses desktop's non-`@Composable` classpath `Font("font/…ttf")` (fonts in
  `composeUi/src/jvmMain/resources/font`). This avoided Compose Resources'
  `@Composable Font()`, leaving all **186** static `AppTypography.*` call sites
  untouched.
- Moved `AppTypography`, `JrrTheme`, `AlphabetIndexBar` to `composeUi/commonMain`;
  deleted the old Android `AppFonts.kt` and the now-duplicate `androidApp/res/font`.
  The whole `core.theme` package now lives in `:composeUi`.
- Verified: `:composeUi` (both targets) + `:androidApp:assembleDebug` +
  `:desktopApp:compileKotlinJvm` all green.
- _Gotcha:_ a KDoc containing `font/*.ttf` opened a **nested block comment**
  (`/*`), silently breaking the file's `expect` parse — reworded to avoid `/*`.

**Increment 3 — Coil 2→3 + first shared components ✅ DONE**
- Migrated **Coil 2 → Coil 3** (`io.coil-kt.coil3`, version `3.1.0`): catalog
  entries, all 6 `AsyncImage` imports (`coil.compose` → `coil3.compose`), and the
  loader — `JrrApplication` now implements Coil 3's `SingletonImageLoader.Factory`
  with an `OkHttpNetworkFetcherFactory` over the existing **trust-all**
  `OkHttpClient` (JRiver self-signed cert preserved). `:composeUi` commonMain gets
  `coil3-compose`; the network fetcher stays host-side.
- Decoupled the inline Android local-artwork lookup (`LocalContext` +
  `filesDir/downloads/art_<key>.jpg`) behind a commonMain
  [`ArtworkResolver`](../composeUi/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/ArtworkResolver.kt)
  CompositionLocal — Android provides the file-checking impl in `MainActivity`;
  desktop uses the default identity resolver (remote URL) until desktop downloads
  exist.
- Moved `MiniPlayer` + `VinylSleeve` to `composeUi/commonMain`.
- Verified: `:composeUi` (both), `:androidApp:assembleDebug`,
  `:desktopApp:compileKotlinJvm` all green. Runtime image-load on a device still
  to be smoke-tested.
- _Gotcha:_ `LocalContext`/`java.io.File` couplings were hidden from the earlier
  import audit (it filtered out all `androidx.compose.*`) — caught at compile.

**Increment 4 — Toast/Intent abstraction ✅ DONE**
- Added a commonMain
  [`PlatformUi`](../composeUi/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/PlatformUi.kt)
  CompositionLocal (`showToast`, `shareText`). Converted all **14** `Toast.makeText`
  calls (8 files) to `platformUi.showToast(...)` and both `Intent.ACTION_SEND`
  share actions (InfoDialog, SettingsScreen) to `platformUi.shareText(...)`;
  removed the `android.widget.Toast` / `android.content.Intent` /
  `LocalContext` imports from those files.
- Android impl provided in `MainActivity` (`Toast` + share-sheet `Intent`), wired
  via `CompositionLocalProvider`.
- No files moved yet (screens still carry the icons coupling), but they are now
  **icons-only** and ready for the final move.
- Verified: `:composeUi` + `:androidApp:assembleDebug` green.
- _Note:_ `LibraryScreen` keeps a `LocalContext` for `context.appContainer.database`
  (an Android-DI access, line ~1432) — to be hoisted when that screen moves.

**Increment 5 — icons + move all remaining screens ✅ DONE (Phase 1 complete)**
- **Icons (Decision D resolved → D1):** added `compose.materialIconsExtended`
  (JetBrains MP artifact) to `:composeUi`. Same `androidx.compose.material.icons`
  package as the androidx artifact, so every screen's icon import resolved
  unchanged — no vendoring needed.
- **Hoisted `LibraryScreen`'s DB access** behind a commonMain
  [`LocalDatabase`](../composeUi/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/LocalDatabase.kt)
  CompositionLocal (provided by `MainActivity`), replacing `context.appContainer.database`.
- **Resolved further hidden couplings the import audit missed** (all were
  fully-qualified, so not caught by import greps):
  - `LocalMcwsClient` (was in androidApp `core.di`) → moved to `:composeUi`.
  - `androidx.activity.compose.BackHandler` → commonMain `expect`/`actual`
    [`BackHandler`](../composeUi/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/BackHandler.kt)
    (Android = system back, desktop = no-op).
  - `java.util.Locale` mm:ss/`%02d` formatters (6 sites) → multiplatform `padStart`.
  - `java.util.UUID` (ServerManager, id only — dedup is by host/port) →
    `kotlin.uuid.Uuid`; `System.currentTimeMillis()` → `expect`/`actual`
    [`nowEpochMillis()`](../composeUi/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/Time.kt).
  - Added `androidx.lifecycle.*` + `androidx.room.runtime` to `:composeUi`
    (sharedLogic kept them `implementation`, so the VM/`RoomDatabase` supertypes
    weren't transitively visible).
- **Moved all remaining `presentation/{screens,shell}` + `InfoDialog`** (10 files)
  to `composeUi/commonMain`. `androidApp` now holds **only** host/platform glue
  (Application, MainActivity, AppContainer/DI, SSL, Media3 playback, WorkManager,
  FileProvider); all **27** UI files live in `:composeUi/commonMain`.
- Verified: `:composeUi` (jvm + android), `:androidApp:assembleDebug`,
  `:desktopApp:compileKotlinJvm` all green. **Phase 1 exit criteria met.**
- _Caveat:_ Android runtime not yet smoke-tested on a device; desktop renders the
  Phase-0 spike until the Phase 2 host wires the real screens.

### Phase 2 — Desktop host wiring ✅ COMPLETE

- `DesktopAppContainer` ✓ — builds `database` (jvm `DatabaseBuilder`), `McwsCore`
  (`.mcwsClient`/`.serverRepository`), `LibraryRepository`, `AudioPlayerFacade`,
  and the `DesktopPlayerEngine` stub — mirroring
  [AppContainer.kt](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/core/di/AppContainer.kt)
  minus Context/WorkManager.
- `DesktopSettings` ✓ — backed by `java.util.prefs.Preferences`; implements
  `MainShellSettings` + adds the local-audio-quality key the facade persists.
- `DesktopPlayerEngine` ✓ — `LocalPlayerEngine` stub (Decision A) holding
  queue/index/mode `StateFlow`s so the UI binds; no audio output until Phase 4.
- **`MainShell` promoted to shared** ✓ — moved from `MainActivity.kt` into
  [composeUi/.../presentation/MainShell.kt](../composeUi/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/MainShell.kt),
  decoupling the last two Android-isms: Toast → `LocalPlatformUi`, and
  `LocalConfiguration.screenWidthDp` → a `windowWidthDp: Int` parameter (Android
  passes `screenWidthDp`; desktop measures the window with `BoxWithConstraints`
  and passes `maxWidth.value.toInt()`). Android `MainActivity` now just builds
  DI + provides the four CompositionLocals and calls the shared shell.
- Real `AppDeps` factories in `Main.kt` ✓ (same lambdas as the Android host).
  The container + component tree are built inside `application { remember { … } }`
  so Decompose sees the AWT EDT as its main thread.
- **Gotcha fixed:** plain JVM has no `Dispatchers.Main` provider, so the facade's
  `CoroutineScope(Dispatchers.Main + …)` threw at startup. Added
  `kotlinx-coroutines-swing` to `:desktopApp` (backs `Dispatchers.Main` with the
  EDT — the same thread Compose Desktop composes on).
- **Gotcha fixed:** Room types leak through `DesktopAppContainer`'s API and the
  ViewModels extend `androidx.lifecycle.ViewModel`; both are non-transitive
  `implementation` deps of `:sharedLogic`, so `:desktopApp` declares
  `room-runtime`, `sqlite-bundled`, and `lifecycle-viewmodel` directly.
- **Verified:** `:androidApp:compileDebugKotlin` + `:desktopApp:compileKotlinJvm`
  green; `:desktopApp:run` boots — container constructs, Room opens
  `%LOCALAPPDATA%\jrr_database.db`, auto-connect restores the Offline zone, and
  the real shared screens render through `MainShell` (Player tab, NowPlaying VM).
- **Deferred:** `AppLogger.configure(isDebug = …)` is not yet called in `main()`
  (logging already works via the default config); fold into Phase 5 polish.
- **Exit criteria (remaining for full sign-off):** connect to a live JRiver
  server, browse library, control a zone (remote playback) — needs a running
  server to exercise; the offline path + navigation are confirmed working.

### Phase 3 — Images

- Migrate to Coil 3 in `:composeUi` commonMain; desktop + Android both use
  `coil-network-ktor` over the shared Ktor client incl. trust-all SSL
  (replaces the OkHttp `ImageLoaderFactory` in
  [JrrApplication.kt:28-39](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/JrrApplication.kt)).
- **Exit criteria:** album art renders on desktop and still renders on Android.

### Phase 4 — Desktop local playback (VLCJ) ✅ COMPLETE (pending live-server audio check)

- Real `LocalPlayerEngine` via **VLCJ** (libvlc) ✓ — `DesktopPlayerEngine`
  rewritten from the Phase 2 stub, implementing all interface methods. It is the
  JVM analogue of the Android ExoPlayer
  [LocalPlayerHandler.kt](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/playback/LocalPlayerHandler.kt):
  single player + app-managed queue, streaming the MCWS `File/GetFile` endpoint
  for the active server, honouring the user's `LocalAudioQuality`.
  - **Stream URL** built from `serverRepository.activeServer` exactly like
    Android: `…/MCWS/v1/File/GetFile?File=<key>&FileType=Key&Playback=1&<quality.mcwsParams>&Token=<t>`.
    Returns null (no-op) when offline.
  - **Advance logic** (`onTrackFinished`) honours repeat (TRACK / PLAYLIST) and
    shuffle (random next), mirroring ExoPlayer's behaviour.
  - **Threading:** libvlc fires events on a native thread; re-entering the player
    from there can deadlock, so track-advance is bounced onto a single-thread
    `advanceExecutor`. Volume is reapplied in the `playing` event (libvlc resets
    it per media).
  - **Graceful degradation:** libvlc is discovered lazily on first *real*
    playback; if absent, the engine logs one Error and no-ops (remote zone
    control unaffected). Passive lifecycle calls (`stop`/`pause`/`clearQueue`/
    `setVolume`/position polls) are guarded on `isInitialized()`, so a
    remote-only session never loads libvlc. `release()` (wired to the window
    close) tears it down only if it was started.
  - Container passes `serverRepository` + a `LocalAudioQuality` provider (reads
    `DesktopSettings`) into the engine.
- **Verified:** `:desktopApp:compileKotlinJvm` green; `:desktopApp:run` boots,
  libvlc initialises on demand ("VLCJ audio player initialised") on a machine
  with VLC installed, and stays unloaded at boot for the offline/remote path.
- libvlc natives are resolved from the **system VLC install** at runtime;
  bundling them in the installer is deferred to Phase 5 (see §6 risk).
- **Exit criteria (remaining for sign-off):** confirm audio output by streaming
  a track from a live JRiver server on Windows — needs a server + credentials to
  exercise end-to-end (engine wiring, URL build, and libvlc load are confirmed).

### Phase 5 — Packaging ✅ COMPLETE

- jpackage config ✓ — `nativeDistributions` sets `packageName`/`version`/
  `description`/`vendor`/`copyright`, `targetFormats(Msi, Exe)`, and Windows
  metadata (`menuGroup`, start-menu + desktop shortcut, `perUserInstall`, a fixed
  `upgradeUuid` for in-place upgrades). `includeAllModules = true` so JNA/VLCJ
  reflection + Room + Skiko all resolve without hand-enumerating JDK modules.
  (Branded `.ico` is a TODO — the DSL line is stubbed in; jpackage uses a default
  icon meanwhile.)
- **libvlc natives bundled** ✓ (Decision: self-contained installer). The
  `syncVlcNatives` Gradle task stages `libvlc.dll`, `libvlccore.dll`, and the
  ~135MB `plugins/` tree from a VLC install (resolution: `-PvlcHome=…` >
  `VLC_HOME` env > default `C:/Program Files/VideoLAN/VLC`) into the Compose
  app-resources dir (`windows-x64/vlc/`); it's wired as a `dependsOn` of
  `prepareAppResources`. At runtime `DesktopPlayerEngine.configureBundledNatives`
  points JNA at `<resources>/vlc` (via `compose.application.resources.dir`), so
  **no system VLC is required by the end user**; running unpackaged falls back to
  system discovery. The task is skipped (with a warning) if no VLC is found, so
  the build still works on machines without VLC (the app then needs a system VLC
  at runtime).
- **Verified:** `:desktopApp:createDistributable` produces a runnable app image
  with the natives at `JRRDesktop/app/resources/vlc/` (libvlc.dll + 368 plugin
  files); the packaged `JRRDesktop.exe` launches without crashing. Compose
  auto-downloads WiX 3.11, so `:desktopApp:packageMsi` produces
  **`JRRDesktop-1.0.0.msi` (~199 MB)** — the bundled JVM runtime + libvlc. No
  separate WiX install needed.
- **Exit criteria met:** `./gradlew :desktopApp:packageMsi` produces an installer.

#### Building the installer

```bash
# Self-contained MSI (bundles a JRE + libvlc; no system VLC needed by users).
# Point -PvlcHome at your VLC install if it isn't the default path.
./gradlew :desktopApp:packageMsi            # -> build/compose/binaries/main/msi/
./gradlew :desktopApp:packageExe            # NSIS-style .exe installer
./gradlew :desktopApp:createDistributable   # unpacked app image (fast, for testing)
```

Remaining polish (non-blocking): add a branded `.ico`, trim `includeAllModules`
to a `modules(...)` allowlist to shrink the image, and add macOS/Linux native
sources to `syncVlcNatives` if those targets are packaged later.

---

## 5. Key decisions to confirm before starting

### Decision A — Desktop local playback scope ✅ DECIDED: VLCJ from the start

This app is a **JRiver remote** (primary function: controlling zones on a remote
Media Center via MCWS), but v1 will ship **real local playback via VLCJ**
(libvlc) rather than a stub. Phase 2 still constructs the facade with a
throwing/no-op stub so the host comes up early; Phase 4 replaces it with the real
VLCJ engine. Trade-off accepted: libvlc adds native libraries to bundle and a
larger installer, in exchange for robust codec/format coverage.

_(Considered and rejected: A1 stub-only for v1 — defers a core feature; A3 JavaFX
Media — too limited on codecs/containers.)_

### Decision B — One shared host composable or two?

The `MainShell` glue ([MainActivity.kt:150-268](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/MainActivity.kt))
has minor Android touches (Toast, `LocalConfiguration`). Either (B1) lift it to
`commonMain` with an `expect fun showToast` + window-size param, or (B2) keep a
thin per-platform `MainShell` and share only the inner `MainContent`/children.
**B1** maximizes reuse; **B2** is less coupling-surgery up front.

### Decision C — Coil 3 migration ✅ DECIDED: migrate Android too

Coil 3 is required for desktop images, and **Android migrates Coil 2→3 at the
same time** so there is a single shared image stack in `:composeUi` over the
shared Ktor client (incl. trust-all SSL for self-signed JRiver servers). This
replaces the OkHttp `ImageLoaderFactory` in
[JrrApplication.kt:28-39](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/JrrApplication.kt).
_(Rejected: Coil 3 desktop + Coil 2 Android — two divergent image stacks.)_

### Decision D — `material-icons-extended`

Current dep is the androidx artifact ([libs.versions.toml:66](../gradle/libs.versions.toml)).
Only ~4 icons are used in the shell ([MainActivity.kt:11-13](../androidApp/src/main/kotlin/com/jrr/jrrkmp_native_ui/MainActivity.kt),
`Home/List/PlayArrow/Settings`). Options: (D1) JetBrains MP icons-extended
artifact; (D2) vendor the handful of `ImageVector`s into `composeUi` and drop the
dependency. D2 is lighter and avoids the deprecated-artifact churn. (Audit screen
files for any further icon usage during Phase 1.)

---

## 6. Risks & unknowns

- **Compose Resources font migration** touches `AppTypography`/`AppFonts`
  globally; a regression shows as missing fonts on **both** platforms.
  Mitigation: gate Phase 1 on the Android app still rendering correctly.
- **Coil 3 API differs** from Coil 2 (`AsyncImage` model, image loader setup).
  Self-signed-cert handling must be re-expressed through `coil-network-ktor`.
- **`material-icons-extended` for MP** has been deprecated in recent Compose
  releases — D2 (vendor) avoids depending on its continued availability.
- **`compose-material3` version** is pinned separately (`material3` ref,
  [libs.versions.toml:46](../gradle/libs.versions.toml)) — confirm the chosen
  version publishes desktop artifacts compatible with `composeMultiplatform 1.11.0`.
- **Window-size adaptivity**: screens assume a phone/large split keyed on
  `screenWidthDp`; desktop window resizing must feed `BoxWithConstraints` so the
  `LargeScreenShell`/split-pane paths behave on small windows.
- **VLCJ native deps**: VLCJ requires a libvlc runtime on the host. The
  `packageMsi` distribution must bundle the libvlc binaries (or document an
  install dependency); plugin discovery paths differ across Windows setups and
  need testing on a clean machine.
- **No Apple impact intended**: all changes are additive (`jvm()` target, new
  modules). The shared `commonMain` API is untouched, so the SKIE/SwiftUI iOS
  **and** macOS hosts (both bound to `appleMain`) are unaffected — but run an
  Apple framework build in CI after the module split to confirm.

---

## 7. Verification strategy

- Per-phase exit criteria above (Gradle tasks + manual smoke).
- Regression gates: `:androidApp:assembleDebug` after Phase 1; an Apple
  (iOS + macOS) framework assemble in CI after the module split.
- New runnable target: `:desktopApp:run`; installer via
  `:desktopApp:packageMsi`.
- `:sharedLogic:compileKotlinJvm` already green (groundwork phase, verified on
  the `main`-rebased base alongside the macOS target).

---

## 8. Explicitly out of scope (v1)

- Native Win32/WinUI/Fluent look-and-feel (this is Skia-rendered Compose; a
  Fluent theme like `compose-fluent-ui` could be layered later).
- WorkManager-style background downloads (desktop uses coroutines; defer or
  reimplement).
- Android Auto / CarPlay parity (mobile-only).
- Auto-update / code signing of the installer.

---

## Appendix A — Why `jvm()`, not `mingwX64()`

A Kotlin/Native Windows target (`mingwX64`) was attempted first and abandoned:
core `commonMain` dependencies **publish no `mingw_x64` artifacts** — Gradle
resolution FAILs for `androidx.room:room-runtime`,
`androidx.sqlite:sqlite-bundled`, `com.arkivanov.decompose`, and all
`com.arkivanov.essenty:*` modules (they ship android/jvm/ios/macos/linux only).
Since these are `api`/`implementation` deps in `commonMain`, no `actual`
implementations can make the native Windows target link. The supported route for
a Windows desktop KMP app is the **JVM desktop target**, which all these
libraries support. The groundwork branch therefore uses `jvm()` with JVM
`actual`s for `DatabaseBuilder`
([DatabaseBuilder.jvm.kt](../sharedLogic/src/jvmMain/kotlin/com/jrr/jrrkmp_native_ui/data/db/DatabaseBuilder.jvm.kt))
and `createPlatformHttpClient`
([PlatformHttpClient.jvm.kt](../sharedLogic/src/jvmMain/kotlin/com/jrr/jrrkmp_native_ui/data/api/PlatformHttpClient.jvm.kt),
OkHttp engine).
