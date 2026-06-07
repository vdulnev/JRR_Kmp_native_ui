# JRR KMP Native UI

This is a Kotlin Multiplatform project designed to provide a cohesive remote control and playback experience across a wide range of platforms. 

The architecture is split between **Shared Business Logic** (Kotlin Multiplatform), **Shared Compose UI** (Android/Desktop), and **Native Apple UI** (SwiftUI for iOS/tvOS).

## Project Structure & Targets

### Shared Code
* **[/sharedLogic](./sharedLogic)**: The core Kotlin Multiplatform module containing the business logic, networking (MCWS), database (Room), state management, and ViewModels. It is compiled for Android, JVM (Desktop), iOS, and tvOS.
* **[/composeUi](./composeUi)**: Shared Compose Multiplatform UI code used by the Android, Android TV, and Desktop targets.

### Android & JVM Targets
* **[/androidApp](./androidApp)**: The Android mobile application entry point.
* **[/androidTvApp](./androidTvApp)**: The Android TV application entry point, utilizing Compose for TV.
* **[/androidCore](./androidCore)**: Shared Android-specific modules and services utilized by both the mobile and TV Android applications.
* **[/desktopApp](./desktopApp)**: The JVM-based Desktop application entry point for macOS, Windows, and Linux.

### Apple Targets
* **[/iosApp/iosApp](./iosApp/iosApp)**: The native iOS application entry point built with SwiftUI. It consumes the `sharedLogic` framework natively (enhanced via SKIE).
* **[/iosApp/tvOSApp](./iosApp/tvOSApp)**: The native tvOS (Apple TV) application entry point built with SwiftUI, optimized for the Siri Remote and television displays.

## Running the Apps

Use the run configurations provided by the run widget in your IDE's toolbar (Android Studio / IntelliJ IDEA). Alternatively, use the following commands:

- **Android Mobile**: `./gradlew :androidApp:assembleDebug` or run directly via the IDE.
- **Android TV**: `./gradlew :androidTvApp:assembleDebug` or run directly via the IDE.
- **Desktop (JVM)**: `./gradlew :desktopApp:run`
- **iOS & tvOS**: Open the Xcode project located in `/iosApp/iosApp.xcodeproj`, select the `iosApp` or `tvOSApp` scheme, and run it.

## Running Tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- **Android Tests**: `./gradlew :sharedLogic:testAndroidHostTest`
- **iOS Tests**: `./gradlew :sharedLogic:iosSimulatorArm64Test`
- **tvOS Tests**: `./gradlew :sharedLogic:tvosSimulatorArm64Test`
- **Desktop/JVM Tests**: `./gradlew :sharedLogic:jvmTest`