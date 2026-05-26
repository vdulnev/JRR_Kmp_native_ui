# Singletons → Ordinary classes — refactoring plan

## Goal

Replace the project's globally-accessible singletons (Kotlin `object` and Swift
`static let shared`) with ordinary classes constructed once at the app entry
point and passed down through normal constructor / parameter / environment
injection. This makes dependencies explicit, removes hidden coupling, and lets
us swap implementations in tests.

## Inventory

### Stateful singletons — refactor (5)

These hold mutable state or wire up real OS resources. They're the actual
problem.

| # | Singleton | Lang | File | Why it's a problem |
|---|---|---|---|---|
| 1 | `McwsClient` | Kotlin `object` | `sharedLogic/.../data/api/McwsClient.kt` | Mutable `activeServerFlow`, used 47× across shared + apps. Effectively global mutable state for "which server are we talking to". |
| 2 | `JrrDependencies` (Android) | Kotlin `object` | `androidApp/.../JrrDependencies.kt` | Lazy service locator with `synchronized` init. 65 call sites between Android + iOS. |
| 3 | `JrrDependencies` (iOS) | Swift `static let shared` | `iosApp/iosApp/Core/JrrDependencies.swift` | Same shape as the Android one — Swift twin. |
| 4 | `PlaybackStateObserver` | Swift `static let shared` | `iosApp/iosApp/Core/PlaybackStateObserver.swift` | Holds Combine `@Published` state + flow subscriptions. Lifetime tied to the app, not to any particular view. |
| 5 | `NowPlayingCoordinator` | Swift `static let shared` | `iosApp/iosApp/Playback/NowPlayingCoordinator.swift` | Owns `MPRemoteCommandCenter` handlers. Configured once at app start. |
| 6 | `DownloadManager` | Swift `static let shared` | `iosApp/iosApp/Data/DownloadManager.swift` | Background `URLSession` + active-downloads dict. Stateful and OS-bound. |

> `JrrAsyncImage.imageCache` (`static let shared = NSCache<NSURL, UIImage>()`)
> is a Swift idiom for a process-wide image cache. Defer; not in scope.

### Pure utility "objects" — leave or do trivially (5)

These are stateless function-bags. They look like singletons but cause no
coupling because they hold no state. Useful to flag — refactor only if the goal
is a *strict* no-`object` policy.

| Singleton | File | Notes |
|---|---|---|
| `McwsXmlParser` | `sharedLogic/.../data/api/McwsXmlParser.kt` | Pure XML parsing. Convert to top-level functions in same file or leave as-is. |
| `WebPlayLookup` | `sharedLogic/.../data/api/WebPlayLookup.kt` | Single `suspend fun lookup(...)`. Same. |
| `AudioPlayerFacadeFactory` | `sharedLogic/.../playback/AudioPlayerFacadeFactory.kt` | Single `fun create(...)`. Will become redundant once the iOS container takes over — delete after Phase 3. |
| `SslHelper` | `androidApp/.../core/network/SslHelper.kt` | Static-helper utility. |
| `AppColors`, `AppSpacing`, `AppFonts`, `AppTypography` | `androidApp/.../core/theme/` | Constants / theme namespaces. The Compose idiom is `MaterialTheme.colorScheme.*`; if we ever Material-3-ify properly these go away. Defer. |

## Target architecture

A single `AppContainer` per platform, instantiated **once** at the app entry
point, holding every long-lived service as a non-optional `val` / `let`.
Passed down explicitly — no statics.

### Android

```kotlin
// new file: androidApp/.../JrrApplication.kt
class JrrApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}

// new file: androidApp/.../core/di/AppContainer.kt
class AppContainer(context: Context) {
    val database: JrrDatabase = createDatabase(DatabaseBuilder(context).createBuilder())
    val serverRepository: ServerRepository = ServerRepository(database)
    val mcwsClient: McwsClient = McwsClient(serverRepository.activeServer)
    val libraryRepository: LibraryRepository = LibraryRepository(database, mcwsClient)
    val localPlayerHandler: LocalPlayerHandler = LocalPlayerHandler(context, serverRepository)
    val facade: AudioPlayerFacade = AudioPlayerFacade(
        database = database,
        localPlayerEngine = localPlayerHandler,
        serverRepository = serverRepository,
        mcwsClient = mcwsClient,
        saveLastActiveZoneId = { prefs(context).edit().putString("last_active_zone_id", it).apply() },
        loadLastActiveZoneId = { prefs(context).getString("last_active_zone_id", null) },
    )
}
```

`MainActivity` reads `(application as JrrApplication).container` and hands the
relevant pieces to `MainShell` / view models. No global access elsewhere.

### iOS

```swift
// iosApp/iosApp/Core/AppContainer.swift
final class AppContainer {
    let database: JrrDatabase
    let serverRepository: ServerRepository
    let mcwsClient: McwsClient
    let libraryRepository: LibraryRepository
    let localPlayerEngine: IosLocalPlayerEngine
    let facade: AudioPlayerFacade
    let playbackStateObserver: PlaybackStateObserver
    let nowPlayingCoordinator: NowPlayingCoordinator
    let downloadManager: DownloadManager

    init() { /* wire everything once */ }
}

// iosApp/iosApp/iOSApp.swift
@main
struct iOSApp: App {
    @State private var container = AppContainer()

    var body: some Scene {
        WindowGroup {
            ContentView(container: container)
                .environment(container)              // or .environmentObject
        }
    }
}
```

SwiftUI views pull what they need via `@Environment(AppContainer.self)` (or
explicit init parameters for views that are easy to construct). No `.shared`
anywhere.

## Phased plan

Each phase is independently shippable — the app stays runnable after every
phase. Build green on both platforms is a gate between phases.

### Phase 0 — Preparation (no behavior change)

- Add a `core/di/` package on Android and an `iosApp/iosApp/Core/Container/`
  group on iOS.
- Pick one initial entry-point file to host the new container (`AppContainer`)
  so subsequent phases just keep adding to it.

### Phase 1 — Convert `McwsClient` (Kotlin `object` → `class`)

**Files touched (shared module):**

- `sharedLogic/.../data/api/McwsClient.kt` — change `object` → `class`,
  constructor takes `activeServerFlow: StateFlow<McwsServerData?>`. Drop the
  `initialize(...)` method.
- `sharedLogic/.../data/repository/LibraryRepository.kt` — add `mcwsClient`
  constructor parameter, swap `McwsClient.foo()` → `mcwsClient.foo()`.
- `sharedLogic/.../data/repository/ServerRepository.kt` — same (uses
  `McwsClient.httpClient` directly twice).
- `sharedLogic/.../playback/McwsRemotePlayerHandler.kt` — constructor takes
  `mcwsClient`, switch all 13 call sites.
- `sharedLogic/.../playback/AudioPlayerFacade.kt` — constructor takes
  `mcwsClient`, switch the ~8 call sites; pass it through to
  `McwsRemotePlayerHandler` it instantiates.
- `sharedLogic/.../domain/model/PlaybackModels.kt` — **delete** `Track.imageUrl`.
  The model becomes pure data again.
- `sharedLogic/.../presentation/viewmodel/NowPlayingViewModel.kt` —
  constructor takes `mcwsClient`; build `imageUrl` here instead of in the model.

**Call sites that read `track.imageUrl` (need fixup):**

- `iosApp/.../Presentation/Views/AlbumDetailView.swift:165` — compute via
  `container.mcwsClient.buildImageUrl(fileKey:)` against `tracks.first?.fileKey`.
- `iosApp/.../Presentation/Views/LibraryView.swift:627` — same pattern.

**Verification:**

- `./gradlew :sharedLogic:compileKotlinIosSimulatorArm64 :androidApp:compileDebugKotlin`
- iOS app launches, mini-player art still shows, album-detail art still shows.

### Phase 2 — Android container

**Files touched:**

- Create `androidApp/.../JrrApplication.kt` (Application subclass).
- Create `androidApp/.../core/di/AppContainer.kt`.
- Register `JrrApplication` in `androidApp/src/main/AndroidManifest.xml`
  (`android:name=".JrrApplication"`).
- Rewrite `androidApp/.../MainActivity.kt` to read the container from
  `application` and pass dependencies down.
- Update `androidApp/.../playback/service/PlaybackService.kt` and any other
  `JrrDependencies.get*` callers.
- **Delete** `androidApp/.../JrrDependencies.kt`.

**Verification:**

- Build, launch, connect to a server, play a track, force-stop, relaunch — make
  sure background-service path still works (Application subclass init order
  matters).

### Phase 3 — iOS container

Sub-phases — convert one singleton at a time, each compiles + runs.

**3a. `JrrDependencies` (Swift) → `AppContainer`**

- Create `iosApp/iosApp/Core/AppContainer.swift` containing the current init
  logic of `JrrDependencies.swift`.
- Construct in `iOSApp.swift` via `@State` (so the lifetime tracks the app).
- Inject via SwiftUI `Environment`. Update `ContentView` and downstream views
  to read from `@Environment(AppContainer.self)`.
- Touched: `iOSApp.swift`, `ContentView.swift`, every view that currently
  reads `JrrDependencies.shared.*` (≈25 sites).
- **Delete** `JrrDependencies.swift` at end of sub-phase.

**3b. `PlaybackStateObserver` → instance owned by container**

- Drop `static let shared`; make `init` accessible.
- Move construction into `AppContainer.init` so it can capture `facade` and
  `database`.
- Call sites that referenced `.shared` (5 of them — see Inventory) get the
  instance from the environment.

**3c. `NowPlayingCoordinator` → instance owned by container**

- Drop `static let shared`. Construct in `AppContainer.init`; configure from
  there.
- The only external reference is from `PlaybackStateObserver`, which is now an
  instance — pass it via constructor.

**3d. `DownloadManager` → instance owned by container**

- Drop `static let shared`. Construct in `AppContainer.init`.
- The single external caller is `iOSApp.swift`'s
  `DownloadManager.shared.setup(...)` — that wiring moves into
  `AppContainer.init`.

**Verification after each sub-phase:**

- Xcode build, run, basic smoke test: connect → browse → play → background →
  lock-screen controls work → download a track → backgrounded download
  completes.

### Phase 4 — Pure utility objects (optional)

Only if a strict no-`object` policy is desired. Otherwise skip.

- `McwsXmlParser` → top-level `fun parseResponse(...)` etc. in the same file.
- `WebPlayLookup` → top-level `suspend fun webPlayLookup(...)`.
- `SslHelper` → top-level functions.
- `AudioPlayerFacadeFactory` — already used only by `JrrDependencies` on iOS;
  removed naturally by Phase 3a.
- Theme objects (`AppColors`, `AppSpacing`, `AppFonts`, `AppTypography`) —
  consider moving to a proper `MaterialTheme` extension rather than
  hand-rolling. Separate effort, not part of this plan.

## Risks & open questions

- **Background `PlaybackService` (Android).** It currently gets dependencies
  via `JrrDependencies.get*(context)`. After Phase 2 it'll have to fetch the
  container from `applicationContext as JrrApplication`. Verify the service
  doesn't run before `Application.onCreate()` (it shouldn't, but worth a
  smoke test on cold-start + headset-play intent).
- **`McwsClient.httpClient` shared property.** Both `LibraryRepository` and
  `ServerRepository` reach into `McwsClient.httpClient` directly. After
  Phase 1 they get the `httpClient` through the `mcwsClient` instance — same
  thing, just via a member access. No behavior change.
- **SwiftUI Environment vs explicit init.** Using `@Environment` is the least
  invasive migration. The downside is it's a runtime lookup — if a view is
  used outside the app's view tree (previews) it fails at runtime. Mitigate
  by providing a stub container in `#Preview` blocks.
- **`Track.imageUrl` removal.** Anything currently reading `track.imageUrl`
  (iOS Library + Album Detail) needs the `mcwsClient` instance at the call
  site. Both views already sit inside the SwiftUI tree, so injecting via
  environment is straightforward.
- **Tests.** There are no widget/unit tests in the repo today, so the
  refactor doesn't break any existing test setup — but it *enables* writing
  them, which was the original motivation.

## Sequencing recommendation

Do the phases in order; each is one or two PRs:

1. **Phase 1** — shared `McwsClient` (touches both apps' call sites; biggest
   single-file change but mechanical).
2. **Phase 2** — Android `AppContainer`.
3. **Phase 3a–3d** — iOS, one singleton per PR.
4. **Phase 4** — optional cleanup.

Estimated effort: ~1 day per phase for someone familiar with the code, mostly
mechanical search-and-replace plus a careful pass over the entry points.
