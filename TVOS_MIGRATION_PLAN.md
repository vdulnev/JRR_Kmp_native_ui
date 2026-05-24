# tvOS Platform Migration Plan: JRiver Remote (JRR)

This plan outlines the steps required to add Apple TV (tvOS) support to the JRiver Remote (JRR) Kotlin Multiplatform project.

---

## 1. Architectural Strategy

```
┌────────────────────────────────────────────────────────┐
┌────────────────────────────────────────────────────────┐
│                        UI LAYER                        │
├───────────────────┬───────────────────┬────────────────┤
│    Android App    │      iOS App      │   tvOS App     │
│ (Jetpack Compose) │     (SwiftUI)     │   (SwiftUI)    │
└─────────┬─────────┴─────────┬─────────┴────────┬───────┘
          │                   │                  │
          ▼                   ▼                  ▼
┌────────────────────────────────────────────────────────┐
│                    PLAYBACK ENGINES                    │
├───────────────────┬───────────────────┬────────────────┤
│  Jetpack Media3   │   AVQueuePlayer   │ AVQueuePlayer  │
└─────────┬─────────┴─────────┬─────────┴────────┬───────┘
          │                   │                  │
          ▼                   ▼                  ▼
┌────────────────────────────────────────────────────────┐
│               SHARED APPLE LOGIC LAYER                 │
│                     (`appleMain`)                      │
├────────────────────────────────────────────────────────┤
│  - SQLite Database Builder (iOS / tvOS)                │
│  - FlowObserver (Swift Generic Flow wrapper)          │
│  - AVPlayer Local Engine Implementation                │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│                  SHARED LOGIC LAYER                    │
│                    (`commonMain`)                      │
└────────────────────────────────────────────────────────┘
```

By leveraging Kotlin Multiplatform, we can reuse **100%** of our shared network calls (`McwsClient`), database structures (`JrrDatabase`), and the `AudioPlayerFacade` on tvOS. 

We will create an intermediate `appleMain` source set in Kotlin to share the database builder and flow observers directly between iOS and tvOS targets without code duplication.

---

## Proposed Changes

### Build & Configurations

#### [MODIFY] [sharedLogic/build.gradle.kts](file:///Users/vd/src/JRR_kmp_native_ui/sharedLogic/build.gradle.kts)
1. Add tvOS targets (`tvosArm64()`, `tvosSimulatorArm64()`).
2. Create an intermediate `appleMain` source set.
3. Configure `iosMain` and `tvosMain` to depend on `appleMain`.

```kotlin
kotlin {
    androidTarget()
    
    // iOS targets
    iosArm64()
    iosSimulatorArm64()
    
    // tvOS targets
    tvosArm64()
    tvosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting
        
        // Define Apple shared source set
        val appleMain by creating {
            dependsOn(commonMain)
        }
        
        val iosMain by getting {
            dependsOn(appleMain)
        }
        val tvosMain by getting {
            dependsOn(appleMain)
        }
    }
}
```

#### [MODIFY] Source Set Reorganization
* Move database builder `DatabaseBuilder.ios.kt` to `appleMain/kotlin/.../DatabaseBuilder.apple.kt`.
* Move `FlowObserver.kt` from `iosMain` to `appleMain`.
* Move `AudioPlayerFacadeFactory.kt` and `IosLocalPlayerEngine.kt` to `appleMain`.

---

### Xcode & UI Layer (`iosApp.xcodeproj`)

1. **Create tvOS Target**: Add a new **tvOS -> App** target named `tvApp` to the Xcode workspace.
2. **Link Shared Framework**: Configure the new `tvApp` target to link against the compiled `SharedLogic.framework` output by the KMP gradle build task.
3. **Build Phases**: Add the Run Script build phase to run the `:sharedLogic:embedAndSignAppleFrameworkForXcode` gradle task during tvOS compilation.

---

### SwiftUI UI Layer for Apple TV

tvOS relies entirely on Apple's **Focus Engine** for navigation via the Apple Remote (no direct touch). The UI will be adapted to fit TV screens:

#### 1. Home / Zones View
* Large grid representation of active playback Zones.
* Focus highlights on zone cards with scale-up micro-animations.

#### 2. Now Playing screen
* Large prominent album art.
* Monospaced track progress bar.
* Clean visual VU meters designed for 4K resolutions.

#### 3. TV Library Browser
* Split-screen layout (Categories on the left, items/artists/albums grid on the right).
* TV-optimized grid sizing for album sleeves.

---

## Verification Plan

### Automated Build Verification
Verify tvOS framework compilation and linking:
```bash
# Verify shared KMP compiles for tvOS simulator
./gradlew :sharedLogic:compileKotlinTvosSimulatorArm64

# Build tvOS Target in Xcode
xcodebuild -project iosApp/iosApp.xcodeproj -scheme tvApp -sdk appletvsimulator -configuration Debug build
```

### Manual Verification
1. Launch the app in the Apple TV Simulator.
2. Verify zone discovery and MCWS authentication.
3. Verify remote polling and local AVPlayer audio playback using simulator remote controls.
