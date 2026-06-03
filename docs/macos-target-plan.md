# Adding a native macOS target

Implementation plan for shipping a **real macOS app** (not "Designed for iPad")
from this KMP codebase, reusing the existing SwiftUI UI layer.

## Decisions (locked)

| Decision | Choice |
| --- | --- |
| Architectures | **Apple Silicon only** — `macosArm64` (no `macosX64`) |
| UI strategy | **Reuse the existing SwiftUI views** with `#if os(iOS)` / `#if os(macOS)` guards; reuse the iPad `LargeScreenShell` for the Mac window |
| Minimum macOS | **macOS 14.0 (Sonoma)** |
| App lifecycle | SwiftUI `App` / `WindowGroup` (macOS); iOS keeps its UIKit lifecycle |

## Current architecture (baseline)

- Gradle modules: `:sharedLogic` (KMP), `:androidApp`. iOS is the Xcode project
  `iosApp/` (not a Gradle module); it runs `./gradlew :sharedLogic:embedAndSignAppleFrameworkForXcode`
  in a "Compile Kotlin Framework" build phase.
- `sharedLogic` targets: `iosArm64`, `iosSimulatorArm64`, `androidLibrary`.
  Static framework `SharedLogic`, exports Decompose/Essenty, processed by SKIE.
- Versions: Kotlin 2.3.21, AGP 9.2.1, Room 2.7.0, SKIE 0.10.12, Ktor 3.0.0.
- iOS deployment target 18.2.
- `iosMain` has **3** platform files, all Darwin-portable:
  - `playback/IosLocalPlayerEngine.kt` — pure Kotlin state holder (real audio is
    Swift `CorePlayer`; **no** Apple APIs) → trivially shared.
  - `data/db/DatabaseBuilder.ios.kt` — Room builder using `platform.Foundation`
    document-dir path (available on macOS).
  - `data/api/PlatformHttpClient.ios.kt` — Ktor Darwin engine + `NSURLCredential`
    (available on macOS).
- Swift app: 29 files; ~11 use iOS-only APIs (see Phase 3).

---

## Phase 1 — Shared KMP macOS target (foundation, fully verifiable)

Goal: produce a `SharedLogic` framework for `macosArm64` with no source changes
to business logic.

### 1.1 Restructure source sets → `appleMain`

The 3 `iosMain` files are Darwin-shared. With Kotlin's default hierarchy template,
adding a macOS target auto-creates an `appleMain` source set that is the common
parent of `iosMain` and `macosMain`.

- Create `sharedLogic/src/appleMain/kotlin/…` and **move** all 3 files there
  (keep package paths):
  - `playback/IosLocalPlayerEngine.kt` → consider renaming to
    `AppleLocalPlayerEngine.kt` (optional; not required).
  - `data/db/DatabaseBuilder.ios.kt` → `DatabaseBuilder.apple.kt`
  - `data/api/PlatformHttpClient.ios.kt` → `PlatformHttpClient.apple.kt`
- Verify the `actual` declarations still match the `expect` in `commonMain`
  (they’re unchanged; only the source set moves).

### 1.2 `sharedLogic/build.gradle.kts`

- Add the target to the framework loop:
  ```kotlin
  listOf(
      iosArm64(),
      iosSimulatorArm64(),
      macosArm64(),                      // NEW
  ).forEach { appleTarget ->
      appleTarget.binaries.framework {
          baseName = "SharedLogic"
          isStatic = true
          export(libs.decompose)
          export(libs.essenty.lifecycle)
          export(libs.essenty.state.keeper)
          export(libs.essenty.back.handler)
          export(libs.essenty.instance.keeper)
      }
  }
  ```
  (rename the loop var `iosTarget` → `appleTarget` for clarity.)
- Move the Darwin engine dep from `iosMain` to `appleMain`:
  ```kotlin
  appleMain.dependencies {            // was iosMain
      implementation(libs.ktor.client.darwin)
  }
  ```
  (`appleMain`/`iosMain` are provided by the default hierarchy template; if not
  resolved automatically, declare `val appleMain by getting` and wire
  `iosMain.dependsOn(appleMain)` etc.)
- Add the Room KSP processor for the macOS target:
  ```kotlin
  dependencies {
      add("kspAndroid", libs.androidx.room.compiler)
      add("kspIosSimulatorArm64", libs.androidx.room.compiler)
      add("kspIosArm64", libs.androidx.room.compiler)
      add("kspMacosArm64", libs.androidx.room.compiler)   // NEW
  }
  ```

### 1.3 Verify (do this BEFORE touching Swift)

```bash
# Shared framework links for macOS:
./gradlew :sharedLogic:linkDebugFrameworkMacosArm64
# (SKIE + Room codegen + Ktor Darwin all compile for macosArm64)
```

**Primary risk — Room 2.7.0 on `macosArm64`.** Room KMP advertises native
support, but the exact target list varies by version. If `:linkDebugFrameworkMacosArm64`
fails inside Room/SQLite codegen:
- Confirm `androidx.sqlite:sqlite-bundled` ships a `macosArm64` klib (it should in 2.7).
- If Room genuinely lacks macOS: options are (a) bump Room to the first version
  that adds it, or (b) provide a macOS `actual` `DatabaseBuilder` over
  `androidx.sqlite` bundled driver directly without the Room Apple artifact.
  Decide once the failure (if any) is seen.

Secondary checks: SKIE 0.10.12 supports `macosArm64` (yes); Ktor `ktor-client-darwin`
supports `macosArm64` (yes).

**Exit criterion:** `linkDebugFrameworkMacosArm64` succeeds and emits
`SharedLogic.framework` under `sharedLogic/build/bin/macosArm64/…`.

---

## Phase 2 — macOS app target in Xcode

Add a second app target to the **existing** `iosApp.xcodeproj` so Swift files can
be shared between the iOS and macOS targets via target membership.

### 2.1 New target

- Target: **`macApp`** (App, macOS, SwiftUI), bundle id
  `com.jrr.jrrkmp-native-ui.JRRKmpnativeui` (or `.mac` suffix if it must coexist),
  `MACOSX_DEPLOYMENT_TARGET = 14.0`, arch arm64.
- Reuse the same asset catalog, fonts (`UIAppFonts` → macOS registers fonts via
  `ATSApplicationFontsPath`/Info.plist `ATSApplicationFontsPath` or programmatic
  `CTFontManagerRegisterFontsForURL`), and `Color`/design system files.

### 2.2 Embed the macOS SharedLogic framework

- Add a "Compile Kotlin Framework" Run Script build phase identical to iOS:
  ```sh
  cd "$SRCROOT/.."
  ./gradlew :sharedLogic:embedAndSignAppleFrameworkForXcode
  ```
  `embedAndSignAppleFrameworkForXcode` resolves the target from Xcode’s
  `$SDK_NAME`/`$ARCHS`; for a macOS arm64 build it selects `macosArm64`. No script
  change needed.
- Set `FRAMEWORK_SEARCH_PATHS` / link `SharedLogic.framework` the same way the iOS
  target does.

### 2.3 App entry + container bootstrap

The iOS app reaches `AppContainer` via `(UIApplication.shared.delegate as? AppDelegate)?.container`.
macOS has no such delegate, and `AppContainer` already creates and **resumes** the
Decompose lifecycle in its own `init` (`LifecycleRegistryExtKt.resume(lifecycle)`),
so it does not depend on a UIKit scene.

- Add `macApp/App.swift` (macOS only):
  ```swift
  import SwiftUI
  import SharedLogic

  @main
  struct JrrMacApp: App {
      @State private var container: AppContainer

      init() {
          AppLogger.shared.configure(isDebug: true, extraWriters: [])
          _container = State(initialValue: AppContainer())
      }

      var body: some Scene {
          WindowGroup {
              ContentView(container: container)
                  .environment(container)
                  .environmentObject(container.playbackStateObserver)
                  .frame(minWidth: 900, minHeight: 640)
          }
          .defaultSize(width: 1200, height: 820)
          .windowToolbarStyle(.unifiedCompact)
      }
  }
  ```
- **Target membership:**
  - macApp **includes:** `ContentView.swift`, all `Presentation/**`, `Core/AppContainer.swift`,
    `Playback/CorePlayer.swift`, `Playback/NowPlayingCoordinator.swift`, design/system files, `App.swift`.
  - macApp **excludes:** `main.swift`, `Core/AppDelegate.swift`, `Core/SceneDelegate.swift`,
    `CarPlaySceneDelegate.swift` (UIKit/scene/CarPlay — iOS only).

---

## Phase 3 — Make the shared Swift compile on macOS

Reuse with guards. Introduce a tiny platform shim, then fix each iOS-only call.

### 3.1 Platform shim — `Presentation/Platform/PlatformCompat.swift` (new, both targets)

```swift
#if os(iOS)
import UIKit
typealias PlatformColor = UIColor
#elseif os(macOS)
import AppKit
typealias PlatformColor = NSColor
#endif

enum Clipboard {
    static func copy(_ s: String) {
        #if os(iOS)
        UIPasteboard.general.string = s
        #elseif os(macOS)
        NSPasteboard.general.clearContents()
        NSPasteboard.general.setString(s, forType: .string)
        #endif
    }
}

func openExternal(_ url: URL) {
    #if os(iOS)
    UIApplication.shared.open(url)
    #elseif os(macOS)
    NSWorkspace.shared.open(url)
    #endif
}
```

### 3.2 `Playback/CorePlayer.swift` — guard `AVAudioSession`

`AVAudioSession` is iOS-only; on macOS `AVPlayer` plays without a session.
Wrap setup + interruption-notification handling:
```swift
#if os(iOS)
import AVFoundation
// ...existing AVAudioSession.sharedInstance() setup + interruption observers...
#endif
```
Everything else in `CorePlayer` (AVPlayer, AVPlayerItem, KVO, timeObserver) is
cross-platform. `MPNowPlayingInfoCenter`/`MPRemoteCommandCenter` in
`NowPlayingCoordinator.swift` are available on macOS → keep (no change expected;
verify `MPRemoteCommandCenter` symbols resolve under macOS SDK).

### 3.3 `ContentView.swift` — UIKit appearance + size class

- `UITabBar.appearance()` / `UITabBarAppearance` / `UIColor` (the tab-bar theming
  block in `init`): wrap in `#if os(iOS)`. On macOS the app uses the
  `LargeScreenShell` path (sidebar), so the bottom `TabView` styling is unused.
- `@Environment(\.horizontalSizeClass)` is iOS-only. Add:
  ```swift
  private var isLarge: Bool {
      #if os(macOS)
      return true                 // Mac window → always the sidebar shell
      #else
      return hSizeClass == .regular
      #endif
  }
  ```
  and guard the `@Environment(\.horizontalSizeClass) private var hSizeClass` declaration with `#if os(iOS)`.
- Any `UIWindow`/`UIScreen` references → none in ContentView after the appearance
  block is guarded (verify).
- Result on macOS: `isLarge == true` → `LargeScreenShell { routedContent() }` (the
  existing iPad layout) renders in the Mac window.

### 3.4 `Presentation/Views/InfoView.swift`

- `UIPasteboard.general.string = …` → `Clipboard.copy(…)`.
- `UIApplication.shared.connectedScenes.first as? UIWindowScene` (used for share/present)
  → guard `#if os(iOS)`; on macOS use `NSWorkspace`/`NSSharingServicePicker` or omit
  the affected affordance behind `#if os(iOS)`.

### 3.5 `Core/AppContainer.swift`

- The doc comment references `UIApplication.shared.delegate`; the class itself is
  cross-platform (Kotlin types + `@Observable`). Confirm no `import UIKit`. If any
  UIKit references exist for the iOS delegate bridge, guard them `#if os(iOS)`.
- macOS gets its container from `JrrMacApp` via `.environment`, so the
  delegate-lookup path is iOS-only.

### 3.6 Fonts

`UIAppFonts` (Info.plist) is iOS. For macOS, register the bundled `.ttf`s — add
`ATSApplicationFontsPath` to the mac target Info.plist (point at the fonts folder)
or register programmatically in `JrrMacApp.init`. Verify `AppFont.inter(...)` /
`ibmPlexMono(...)` resolve on macOS.

---

## Phase 4 — macOS UX polish (after it launches)

- Window: sensible `minWidth/minHeight`, `defaultSize`, remember frame.
- Menus: a minimal `CommandGroup` (Playback: Play/Pause ⌘P, Next/Prev) reusing the
  facade; `LargeScreenShell` already handles hardware-key transport on iPad — verify
  `.onKeyPress` works on macOS or move shortcuts into `Commands`.
- No CarPlay, no scene restoration on macOS.
- App icon (macOS icon set), category, sandbox/entitlements (network client =
  `com.apple.security.network.client`; file access only if needed).

---

## Verification matrix

| Layer | Command / action | Pass = |
| --- | --- | --- |
| Shared (mac) | `./gradlew :sharedLogic:linkDebugFrameworkMacosArm64` | framework links |
| Shared (ios regress) | `./gradlew :sharedLogic:linkDebugFrameworkIosSimulatorArm64` | still links |
| Android regress | `./gradlew :androidApp:assembleDebug` | unaffected |
| macOS app | `xcodebuild -scheme macApp -destination 'platform=macOS,arch=arm64' build` | builds |
| iOS app regress | `xcodebuild -scheme iosApp -sdk iphonesimulator … build` | builds |
| Runtime | launch macApp, connect to JRiver server, browse, **play local audio** | window + playback |

Note: run the mac app **without the Xcode View Debugger** during dev — Xcode injects
a macOS `libViewDebuggerSupport.dylib` that crashes iOS-on-Mac processes; a native
macOS target is unaffected, but keep this in mind if any Designed-for-iPad testing
continues in parallel.

---

## Risks & open questions

1. **Room `macosArm64` support (2.7.0)** — highest risk; gates Phase 1. Mitigation
   in §1.3.
2. **SKIE Swift API parity** — the generated Swift API for `macosArm64` should match
   iOS; confirm `onEnum(of:)`, sealed-class bridging, and exported Decompose types
   resolve in the macOS target.
3. **AVPlayer remote streaming on macOS** — should be identical (AVFoundation); the
   only iOS-specific piece is `AVAudioSession`, guarded out.
4. **`LargeScreenShell` on macOS** — first real render uses this path; expect minor
   layout/focus fixes (it was built for iPad regular width). The earlier
   "Designed for iPad" blank-window observation was on the iOS-on-Mac runtime and is
   not predictive of the native target.
5. **Fonts registration** on macOS (§3.6).
6. **Bundle id coexistence** — if iOS and macOS must install side-by-side on the same
   account, may need distinct ids / a shared App Group.

---

## Ordered task checklist

- [ ] **P1.1** Move 3 `iosMain` actuals → `appleMain`.
- [ ] **P1.2** `build.gradle.kts`: add `macosArm64()` to framework loop; `appleMain`
      gets `ktor-client-darwin`; add `kspMacosArm64` Room compiler.
- [ ] **P1.3** `./gradlew :sharedLogic:linkDebugFrameworkMacosArm64` green (resolve
      Room if needed). Regress iOS + Android links.
- [ ] **P2.1** Add `macApp` SwiftUI target (macOS 14, arm64).
- [ ] **P2.2** Add Kotlin-framework build phase + framework linking.
- [ ] **P2.3** `App.swift` entry; container via `.environment`; set target membership
      (include shared UI/playback; exclude UIKit/scene/CarPlay files).
- [ ] **P3.1** Add `PlatformCompat.swift` shim (Clipboard / openExternal / color).
- [ ] **P3.2** Guard `AVAudioSession` in `CorePlayer.swift`.
- [ ] **P3.3** Guard UITabBar appearance + `horizontalSizeClass`; macOS `isLarge=true`.
- [ ] **P3.4** `InfoView.swift`: clipboard + scene refs.
- [ ] **P3.5** `AppContainer.swift`: confirm/guard any UIKit refs.
- [ ] **P3.6** macOS font registration.
- [ ] **P2/P3** `xcodebuild -scheme macApp … build` green; iOS scheme still green.
- [ ] **P4** Run: window renders `LargeScreenShell`, server connect, local playback.
- [ ] **P4** Menus/shortcuts, icon, entitlements, polish.

---

## Out of scope

- Mac Catalyst (this is a native AppKit/SwiftUI macOS target instead).
- Intel (`macosX64`) — Apple Silicon only per decision.
- Android changes (untouched).
- CarPlay on macOS (N/A).
