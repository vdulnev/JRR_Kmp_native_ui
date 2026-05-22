# Kotlin Multiplatform Migration & Porting Plan: JRiver Remote (JRR)

This document provides a detailed step-by-step roadmap for migrating the native Android (`Jrr_Android`) and iOS (`Jrr_ios`) remote control applications into the Kotlin Multiplatform (KMP) project layout in `/Users/vd/src/JRR_kmp_native_ui/`.

---

## 1. Core Porting Goals & Structure

The target project `/Users/vd/src/JRR_kmp_native_ui/` is structured as a Kotlin Multiplatform project with separate native UI targets:
1. `sharedLogic` (Kotlin Multiplatform module): Shared business logic, database, API client, XML parser, and playback coordinators.
2. `androidApp` (Android Application module): Native Android UI (Jetpack Compose), platform playback services (Media3 ExoPlayer), background downloads (WorkManager), and Android Auto.
3. `iosApp` (Xcode iOS Application target): Native iOS UI (SwiftUI), platform playback engines (AVPlayer), background downloads (URLSession), and CarPlay.
4. `sharedUI` (Kotlin Multiplatform Compose module): Unused (since Android and iOS use native Compose/SwiftUI UI architectures).

---

## 2. Dependency Catalog & Gradle Setup

### 2.1 Gradle Version Catalog: `gradle/libs.versions.toml`
Configure the following libraries to support networking, database, serialization, and coroutines:

```toml
[versions]
# Existing versions
agp = "9.2.1"
android-compileSdk = "36"
android-minSdk = "33"
android-targetSdk = "36"
kotlin = "2.3.21"

# Shared logic dependencies
ktor = "3.0.0"
kotlinx-serialization = "1.7.3"
room = "2.7.0-alpha11" # Room KMP support version
sqlite = "2.5.0-alpha11"
coroutines = "1.9.0"

[libraries]
# Networking (Ktor)
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }

# Serialization
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# Room Multiplatform
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }

# Coroutines
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
room = { id = "androidx.room", version.ref = "room" }
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### 2.2 Shared Logic Configuration: `sharedLogic/build.gradle.kts`
Configure Ktor engine drivers and Room compiler schemas:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    androidLibrary {
        namespace = "com.jrr.jrrkmp_native_ui.sharedLogic"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor-client-core)
            implementation(libs.ktor-client-logging)
            implementation(libs.kotlinx-serialization-json)
            implementation(libs.androidx-room-runtime)
            implementation(libs.sqlite-bundled)
            implementation(libs.kotlinx-coroutines-core)
        }
        androidMain.dependencies {
            implementation(libs.ktor-client-okhttp)
            implementation(libs.kotlinx-coroutines-android)
        }
        iosMain.dependencies {
            implementation(libs.ktor-client-darwin)
        }
    }
}
```

---

## 3. Shared Logic Components (`sharedLogic` in KMP)

### 3.1 Network Client (`McwsClient`)
Port the Retrofit API routes from Android and URLSession queries from iOS to a single Ktor-based client. 

* **Token Interception:** Add an interceptor to inject `Token` query parameter dynamically.
* **Loose JSON Parsing:** Implement standard JSON decoding configurations to coerce drift values (e.g. converting numeric keys back to strings).

```kotlin
val jsonConfiguration = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

class McwsClient(
    private val tokenProvider: () -> String?,
    private val baseUrlProvider: () -> String?
) {
    private val client = HttpClient {
        install(Logging)
        defaultRequest {
            url {
                val token = tokenProvider()
                if (token != null && !parameters.contains("Token")) {
                    parameters.append("Token", token)
                }
            }
        }
    }
    
    // Call endpoints using client.get() or client.post()
}
```

### 3.2 Pure Kotlin XML Tokenizer
Replace platform-native parsers with a lightweight string-tokenizing parser:

```kotlin
object McwsXmlParser {
    fun parseResponse(xmlString: String): McwsResponse {
        var status = "Failure"
        val items = mutableMapOf<String, String>()
        
        // Match status attribute
        val statusRegex = """<Response Status="([^"]+)">""".toRegex()
        val statusMatch = statusRegex.find(xmlString)
        if (statusMatch != null) {
            status = statusMatch.groupValues[1]
        }
        
        // Find all <Item Name="Key">Value</Item> tags
        val itemRegex = """<Item Name="([^"]+)">([^<]*)</Item>""".toRegex()
        itemRegex.findAll(xmlString).forEach { match ->
            val key = match.groupValues[1]
            val value = match.groupValues[2].trim()
            items[key] = value
        }
        
        return McwsResponse(status, items)
    }

    fun parseWebPlayLookup(xmlString: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val tags = listOf("ip", "port", "httpsport", "localiplist")
        
        tags.forEach { tag ->
            val tagRegex = """<$tag>([^<]*)</$tag>""".toRegex()
            tagRegex.find(xmlString)?.let { match ->
                result[tag] = match.groupValues[1].trim()
            }
        }
        return result
    }
}
```

### 3.3 Database Layer (Room Multiplatform)
Declare the entities (`SavedServer`, `Favorite`, `LocalQueueTrack`, `LocalQueueState`, `DownloadedTrack`, `DownloadJob`) inside `commonMain` using Room annotations. Implement native database builders:

```kotlin
// commonMain
@Database(entities = [SavedServer::class, Favorite::class, LocalQueueTrack::class, ...], version = 1)
abstract class JrrDatabase : RoomDatabase() {
    abstract fun savedServerDao(): SavedServerDao
    abstract fun favoriteDao(): FavoriteDao
    // other DAOs
}

expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<JrrDatabase>
```

#### Android database provider (`androidMain`):
```kotlin
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<JrrDatabase> {
    val appContext = (context as Context).applicationContext
    val dbFile = appContext.getDatabasePath("jrr_kmp.db")
    return Room.databaseBuilder<JrrDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
```

#### iOS database provider (`iosMain`):
```kotlin
actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<JrrDatabase> {
    val dbFile = NSHomeDirectory() + "/Documents/jrr_kmp.db"
    return Room.databaseBuilder<JrrDatabase>(
        name = dbFile,
        factory = { JrrDatabase::class.instantiateImpl() } // Generated by Room KMP compiler
    )
}
```

### 3.4 Shared Audio Player Facade
Coordinate playback state, track indices, volume, and 1s/5s polling intervals inside `commonMain`. 

Expose a native handler bridge (`LocalPlayerEngine`) that is implemented inside the Android/iOS apps:

```kotlin
interface LocalPlayerEngine {
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(level: Float)
    fun setQueue(tracks: List<TrackInfo>, startIndex: Int)
    
    val playbackState: StateFlow<PlaybackState>
    val currentTrack: StateFlow<TrackInfo?>
    val currentIndex: StateFlow<Int>
    val volume: StateFlow<Float>
}
```

The facade (`AudioPlayerFacade`) delegates command executions:
* **Remote Zones:** Hits Ktor `/Playback/Play`, `/Playback/Pause` endpoints and schedules `/Playback/Info` polling.
* **Virtual Zones (Local, Offline, CarPlay):** Passes inputs directly to the registered `LocalPlayerEngine` instance and suspends Ktor polling.

---

## 4. Native Application Layer Integration

### 4.1 Android app (`androidApp`)
1. **Compose UI Layouts:** Move all screen compose functions from `Jrr_Android`'s `presentation/` package directly to `androidApp`. Rename resources and drawables to align with Android resources pathing.
2. **ExoPlayer Implementation:** Implement `LocalPlayerEngine` inside `androidApp` wrapping ExoPlayer instances, and register it to the shared `AudioPlayerFacade`.
3. **Android Service & Notification Integration:** Wrap the player facade in a `MediaLibraryService` (using Jetpack Media3 Session), linking lock-screen notifications and Android Auto browser callbacks to active transport flows.
4. **Downloads Worker:** Declare Android WorkManager download loops writing file data streams to the local directory.

### 4.2 iOS app (`iosApp`)
1. **SwiftUI Layouts:** Import `SharedLogic` framework. Move SwiftUI screens and custom 3D rotation views to the iOS workspace.
2. **AVPlayer Implementation:** Implement `LocalPlayerEngine` in Swift conforming to the generated KMP protocol, wrapping an underlying `AVQueuePlayer` or `AVPlayer`.
3. **System Control Center & CarPlay:** Link `MPNowPlayingInfoCenter` and `MPRemoteCommandCenter` controls to the iOS `LocalPlayerEngine` state bindings. Map CarPlay templates (`CPListTemplate`) to KMP database queries.
4. **Downloads Coordinator:** Use iOS `URLSessionConfiguration.background` to handle local track streaming caches.

---

## 5. Migration Checklist & Timeline

### Step 1: KMP Project Configurations
* [ ] Update `gradle/libs.versions.toml` with Room KMP and Ktor versions.
* [ ] Configure dependency plugin imports in `sharedLogic/build.gradle.kts`.
* [ ] Confirm that empty Android App and iOS App build outputs compile correctly.

### Step 2: Shared Domain & Core Data Setup
* [ ] Move KMP Models to `commonMain` package.
* [ ] Port `McwsXmlParser` to KMP using regex-token parsing.
* [ ] Configure `JrrDatabase` and native compiler builder targets.
* [ ] Verify that database migrations and schemas are created in the shared module.

### Step 3: Shared Repositories & Polling
* [ ] Port `ServerRepository` and `LibraryRepository` to commonMain.
* [ ] Implement Ktor API methods inside `McwsClient`.
* [ ] Build KMP `AudioPlayerFacade` polling coroutine loop.

### Step 4: Platform Engine Bridges
* [ ] Write native Android `LocalPlayerEngine` (Media3 ExoPlayer wrapper).
* [ ] Write native iOS `LocalPlayerEngine` (AVPlayer wrapper).
* [ ] Link Android Auto and CarPlay controllers to the shared API repositories.

### Step 5: High-Fidelity UI Integration
* [ ] Copy Android Compose screens and tokens.
* [ ] Copy iOS SwiftUI views and styling modifiers.
* [ ] Bind presentation components to ViewModels querying the shared facade state.

### Step 6: Functional Testing
* [ ] Test offline-mode simulation and local downloads database persistence.
* [ ] Verify Android Auto and CarPlay browser interfaces render correctly under simulators.
* [ ] Confirm self-signed SSL handshake configurations operate without security warnings.
