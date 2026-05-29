# Decompose Migration Plan

> **Status:** Planning document. **No production code is changed by this file.**
> It describes a phased adoption of [Decompose](https://arkivanov.github.io/Decompose/)
> for navigation in this Kotlin Multiplatform app. Every claim below is grounded
> in the files cited inline.

---

## 1. Goal & scope

### What this app does today

Navigation is **hand-rolled and state-based**, centralised in one shared
ViewModel:

- `sharedLogic/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/viewmodel/MainShellViewModel.kt`
  owns a single `MainShellState(activeTab: Int = 1, selectedAlbum: Album? = null, showQueue: Boolean = false, isAutoConnecting: …, autoConnectServerName, hasAttemptedAutoConnect, toastMessage)`.
- Tab indices are **magic ints**: `0=Library, 1=ServerManager, 2=Player/NowPlaying, 3=Zones, 4=Settings`
  (see the `when` blocks in `MainActivity.kt:301` and `ContentView.swift:190`, and
  the init logic in `MainShellViewModel.kt:60`).
- Both platforms collect the *same* shared `state: StateFlow<MainShellState>` and
  render natively: Android via a `when(shellState.activeTab)` switch in the
  `MainShell` composable (`MainActivity.kt:301`), iOS via a SwiftUI `TabView`
  bound to `activeTab` (`ContentView.swift:190`).
- Sub-navigation is **boolean/nullable overlay flags**, not a stack:
  - Library → AlbumDetail is gated on `selectedAlbum != null`
    (`MainActivity.kt:304`, `ContentView.swift:408` `LibraryTabContainerView`).
  - Player → Queue is gated on `showQueue` (`MainActivity.kt:346`,
    `ContentView.swift:379` `PlayerTabContainerView`).

### What migrates

- The **routing/state machine**: tab selection, the Library→AlbumDetail and
  Player→Queue sub-stacks, and auto-connect-driven tab flips. These move into a
  Decompose component tree (`RootComponent`, `LibraryComponent`, `PlayerComponent`)
  living in `sharedLogic/src/commonMain`.
- The host glue on both platforms (`MainShell` composable; `ContentView`'s
  `TabView`/overlay swaps) is rewired to **observe the component tree** instead
  of reading magic ints.

### What explicitly does NOT migrate

- **The screens/views themselves stay byte-for-byte.** `LibraryScreen`,
  `AlbumDetailScreen`, `NowPlayingScreen`, `QueueScreen`, `ZonesScreen`,
  `SettingsScreen`, `ServerManagerScreen` (Android, `androidApp/.../presentation/screens/`)
  and the matching SwiftUI `*View`s (`iosApp/iosApp/Presentation/Views/`) keep
  their current `(viewModel, onXClick)` signatures. We change *who calls them and
  with what callbacks*, not their bodies.
- **The feature ViewModels are not rewritten.** `LibraryViewModel`,
  `AlbumDetailViewModel`, `NowPlayingViewModel`, `QueueViewModel`, `ZonesViewModel`,
  `SettingsViewModel` keep their constructors and logic. Only *where they are
  constructed* changes (today `remember { … }` on Android — `MainActivity.kt:105`
  — and `@State` in `ContentView.init` on iOS — `ContentView.swift:108`).
- **`AudioPlayerFacade`, repos, `McwsClient`, the DI containers** are untouched.
  Components receive the same dependencies the hosts already build
  (`AppContainer.kt`, `iosApp/.../Core/AppContainer.swift`).
- **No Compose Multiplatform on iOS.** iOS stays native SwiftUI (`ContentView.swift`).

### Recommendation: **partial / "navigation-only" Decompose, not full Decompose-UI**

The decisive constraint: **iOS renders with native SwiftUI, not Compose
Multiplatform** (`sharedLogic/build.gradle.kts` builds a `SharedLogic` framework;
`iosApp` is a pure SwiftUI target — `iOSApp.swift`, `ContentView.swift`). That
rules out Voyager / Circuit / Navigation-Compose, which all require Compose UI on
both platforms. Decompose is the right pick precisely because its component tree
is **UI-agnostic pure Kotlin** — only the optional `decompose-extensions-compose`
artifact touches Compose, and it is Android-only here.

We adopt Decompose for **the component tree + `ChildStack` routing only**, and
keep both UIs native. We do **not** pull in Compose Multiplatform for iOS.

---

## 2. Dependencies

Current relevant versions (`gradle/libs.versions.toml`):

| Tool | Version | Line |
| --- | --- | --- |
| Kotlin | `2.3.21` | `libs.versions.toml:14` |
| Coroutines | `1.10.1` | `:16` |
| Serialization | `1.8.0` | `:17` |
| Compose Multiplatform | `1.11.0` | `:12` |
| SKIE | `0.10.12` | `:25` |
| Kermit | `2.1.0` | `:26` |

> **Version-compatibility note.** Kotlin `2.3.x` is very new. Decompose tracks
> Kotlin closely via its Essenty dependency. **Pin the exact Decompose/Essenty
> versions to whatever the Decompose release notes mark as compatible with
> Kotlin 2.3.x at implementation time** — do not copy the numbers below blindly.
> As of writing, the 3.x line (Decompose `3.x` + Essenty `2.x`) is the series to
> target; verify on <https://github.com/arkivanov/Decompose/releases> that the
> chosen tag lists Kotlin 2.3 support before committing. If no 2.3-compatible
> Decompose tag exists yet, this migration is **blocked** until one ships — flag
> that in Phase 0 rather than forcing an incompatible version.

### `libs.versions.toml` additions

```toml
[versions]
# … existing …
decompose = "3.x.y"   # PIN to a Kotlin-2.3-compatible release; verify in release notes
essenty   = "2.x.y"   # Travels with Decompose; usually transitive, declared for state-keeper/back-handler

[libraries]
# … existing …
# Core component tree + ChildStack — pure Kotlin, goes in commonMain.
decompose = { module = "com.arkivanov.decompose:decompose", version.ref = "decompose" }
# Compose integration (Children(...), predictive-back) — ANDROID ONLY, pulls Compose.
decompose-extensions-compose = { module = "com.arkivanov.decompose:extensions-compose", version.ref = "decompose" }
# Essenty StateKeeper / BackHandler / InstanceKeeper / Lifecycle. Usually transitive
# via decompose; declare explicitly only if you reference them directly.
essenty-lifecycle = { module = "com.arkivanov.essenty:lifecycle", version.ref = "essenty" }
essenty-state-keeper = { module = "com.arkivanov.essenty:state-keeper", version.ref = "essenty" }
essenty-back-handler = { module = "com.arkivanov.essenty:back-handler", version.ref = "essenty" }
```

### Where each artifact goes (source sets)

| Artifact | Module / source set | Rationale |
| --- | --- | --- |
| `decompose` (core) | `sharedLogic` **`commonMain`** (`sharedLogic/build.gradle.kts:51`) | The component tree is shared Kotlin used by both platforms. Use `api(...)` like Kermit already is (`:64`) so app modules can name `Value`, `ChildStack`, `ComponentContext` without re-declaring the dep. |
| `decompose-extensions-compose` | `androidApp` **`dependencies`** (`androidApp/build.gradle.kts:14`) | Compose-only; iOS never sees it. Provides `Children(...)` for `MainShell`. |
| `essenty-*` | normally transitive via `decompose` in `commonMain`; add explicitly only where referenced | `StateKeeper`/`BackHandler` are surfaced through `ComponentContext`. |

**iOS gets nothing extra.** It consumes the core `decompose` classes
(`Value`, `ChildStack`, `Child`) through the existing `SharedLogic` framework
(`sharedLogic/build.gradle.kts:28`). No new Gradle entry on the iOS side; SKIE
will export the component interfaces automatically.

> Because `decompose` is added with `api(...)` in `commonMain`, its public types
> are exported into the Obj-C/Swift framework and bridged by SKIE — that is what
> makes the iOS `Value`→SwiftUI bridge (§6) possible.

---

## 3. Component-tree design

Replace the magic-int tab model with a typed component tree. All `*Config` route
types are `@Serializable` so Essenty's `StateKeeper` can persist the back stacks
across process death.

### Route configs (replaces `activeTab: Int`)

```kotlin
// sharedLogic/.../presentation/navigation/RootComponent.kt
@Serializable
sealed interface RootConfig {
    @Serializable data object Library  : RootConfig   // was tab 0
    @Serializable data object Server   : RootConfig   // was tab 1
    @Serializable data object Player   : RootConfig   // was tab 2
    @Serializable data object Zones    : RootConfig   // was tab 3
    @Serializable data object Settings : RootConfig   // was tab 4
}
```

### `RootComponent` — the 5 tabs as a `ChildStack` with `bringToFront`

A `ChildStack` whose stack is manipulated with `bringToFront` gives us the
**iOS tab-bar idiom**: switching tabs keeps each tab's sub-state alive at the
bottom of the stack, and `bringToFront` re-orders rather than recreating —
mirroring today's behaviour where switching away from Library and back **keeps**
`selectedAlbum` (the explicit warning in `MainShellViewModel.kt:165-185`).

```kotlin
class RootComponent(
    componentContext: ComponentContext,
    private val deps: AppDeps,            // facade, repos, mcwsClient, database, settings
) : ComponentContext by componentContext {

    private val log = Logger.withTag("vm:RootNav")   // tag taxonomy: vm:* (CLAUDE.md)

    private val nav = StackNavigation<RootConfig>()

    val stack: Value<ChildStack<RootConfig, RootChild>> =
        childStack(
            source = nav,
            serializer = RootConfig.serializer(),
            initialConfiguration = initialTab(deps.settings),  // see init logic below
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun initialTab(s: MainShellSettings): RootConfig = when {
        s.getLastActiveZoneId() == Zone.Offline.id -> RootConfig.Player   // was 2
        s.getHasSavedServers()                     -> RootConfig.Player   // was 2
        else                                       -> RootConfig.Server   // was 1
    }   // ports MainShellViewModel.kt:60-66

    sealed interface RootChild {
        class Library (val component: LibraryComponent) : RootChild
        class Server  (val component: ServerComponent)  : RootChild
        class Player  (val component: PlayerComponent)  : RootChild
        class Zones   (val component: ZonesComponent)   : RootChild
        class Settings(val component: SettingsComponent): RootChild
    }

    private fun child(config: RootConfig, ctx: ComponentContext): RootChild = when (config) {
        RootConfig.Library  -> RootChild.Library(LibraryComponent(ctx, deps))
        RootConfig.Server   -> RootChild.Server(ServerComponent(ctx, deps, onConnected = ::onConnectSuccess))
        RootConfig.Player   -> RootChild.Player(PlayerComponent(ctx, deps))
        RootConfig.Zones    -> RootChild.Zones(ZonesComponent(ctx, deps))
        RootConfig.Settings -> RootChild.Settings(SettingsComponent(ctx, deps))
    }

    /** Tab tap. Ports MainShellViewModel.selectTab (MainShellViewModel.kt:175). */
    fun selectTab(config: RootConfig) {
        log.d { "selectTab($config) active=${stack.active.configuration}" }
        // "tap active Library tab => pop its inner stack to root" idiom:
        if (config == RootConfig.Library && stack.active.configuration == RootConfig.Library) {
            (stack.active.instance as? RootChild.Library)?.component?.popToRoot()
            return
        }
        nav.bringToFront(config)   // preserves the target tab's sub-stack
    }

    private fun onConnectSuccess() { selectTab(RootConfig.Player) }   // ServerManager onConnectSuccess
    fun onAutoConnected()          { selectTab(RootConfig.Player) }   // auto-connect success → tab 2
    fun onDisconnect()             { deps.facade.…; selectTab(RootConfig.Server) }  // ports disconnect()
}
```

### `LibraryComponent` — its own `ChildStack` (List → AlbumDetail)

`selectedAlbum != null` (`MainActivity.kt:304`) becomes a real two-entry stack.
`Album` is already `@Serializable` (`PlaybackModels.kt:165`), so it rides inside
the config and survives process death for free.

```kotlin
class LibraryComponent(ctx: ComponentContext, private val deps: AppDeps)
    : ComponentContext by ctx {

    @Serializable
    private sealed interface Config {
        @Serializable data object List : Config
        @Serializable data class Detail(val album: Album) : Config
    }

    private val nav = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> = childStack(
        source = nav, serializer = Config.serializer(),
        initialConfiguration = Config.List, handleBackButton = true,
        childFactory = { cfg, childCtx -> when (cfg) {
            Config.List      -> Child.List(libraryVm(childCtx))
            is Config.Detail -> Child.Detail(albumDetailVm(childCtx, cfg.album))
        }},
    )

    sealed interface Child {
        class List  (val vm: LibraryViewModel)     : Child
        class Detail(val vm: AlbumDetailViewModel) : Child
    }

    fun openAlbum(album: Album) = nav.push(Config.Detail(album))   // was selectAlbum(album)
    fun back()                 = nav.pop()                         // was selectAlbum(null)
    fun popToRoot()            = nav.popWhile { it !is Config.List }
}
```

### `PlayerComponent` — NowPlaying → Queue

`showQueue` (`MainActivity.kt:346`) becomes a stack:

```kotlin
class PlayerComponent(ctx: ComponentContext, private val deps: AppDeps)
    : ComponentContext by ctx {

    @Serializable private sealed interface Config {
        @Serializable data object NowPlaying : Config
        @Serializable data object Queue : Config
    }
    private val nav = StackNavigation<Config>()
    val stack: Value<ChildStack<*, Child>> = childStack(
        source = nav, serializer = Config.serializer(),
        initialConfiguration = Config.NowPlaying, handleBackButton = true,
        childFactory = { cfg, c -> when (cfg) {
            Config.NowPlaying -> Child.NowPlaying(nowPlayingVm(c))
            Config.Queue      -> Child.Queue(queueVm(c))
        }},
    )
    sealed interface Child {
        class NowPlaying(val vm: NowPlayingViewModel) : Child
        class Queue(val vm: QueueViewModel)           : Child
    }
    fun openQueue() = nav.push(Config.Queue)   // setShowQueue(true)
    fun closeQueue() = nav.pop()               // setShowQueue(false)
}
```

### Mapping the old state onto the tree

| Old `MainShellState` field | New home |
| --- | --- |
| `activeTab: Int` | `RootComponent.stack.active.configuration: RootConfig` |
| `selectedAlbum: Album?` | presence of `LibraryComponent.Config.Detail` on the Library inner stack |
| `showQueue: Boolean` | presence of `PlayerComponent.Config.Queue` on the Player inner stack |
| `isAutoConnecting`, `autoConnectServerName`, `hasAttemptedAutoConnect`, `toastMessage` | **stay as plain state** — these are *not navigation*. Keep them in `RootComponent` as a small `Value<ConnectUiState>` (or keep `MainShellViewModel` for exactly this, see §4 & §12). Decompose does not replace them. |

> **Important:** auto-connect (`MainShellViewModel.kt:70-155`) and toast logic are
> business/UI-feedback state, **not** routing. They must be preserved verbatim.
> Their only navigation side-effect is "flip to Player on success" — that becomes
> `RootComponent.onAutoConnected()`/`selectTab(RootConfig.Player)`.

---

## 4. ViewModel ownership

### Today

- Android: feature VMs are created with `remember { … }` inside the `MainShell`
  composable (`MainActivity.kt:105-133`); `AlbumDetailViewModel` is
  `remember(album) { … }` keyed on the album (`MainActivity.kt:322`). They live
  as long as the composition; `viewModelScope` is the AndroidX `ViewModel` scope.
- iOS: feature VMs are `@State` built in `ContentView.init` (`ContentView.swift:108-150`);
  `AlbumDetailObservable` is re-created via SwiftUI `.id(...)` identity
  (`ContentView.swift:417`).
- These are AndroidX `ViewModel` subclasses (`: ViewModel()` in every VM, e.g.
  `LibraryViewModel.kt`, `AlbumDetailViewModel.kt:58`) using `viewModelScope`.

### After

Each feature VM is constructed **inside its component's `childFactory`** and tied
to the component's Essenty lifecycle, not to a Compose `remember` or SwiftUI
`@State`. Two helpers keep the existing VMs working unchanged:

- **`instanceKeeper`** — retains the VM instance across configuration changes and
  (with `StateKeeper`) process death, replacing `remember`.
- **`coroutineScope()`** (Essenty extension) — a scope cancelled on component
  destroy. We map each VM's `viewModelScope` onto it.

Because the VMs are AndroidX `ViewModel`s, the cleanest adapter is Decompose's
`instanceKeeper.getOrCreate { … }` wrapping a small `InstanceKeeper.Instance` that
holds the VM and calls its `clear()`/`onCleared()` on `onDestroy`:

```kotlin
private fun LibraryComponent.libraryVm(ctx: ComponentContext): LibraryViewModel =
    ctx.instanceKeeper.getOrCreate("library") {
        VmHolder(LibraryViewModel(deps.libraryRepository, deps.facade))
    }.vm

// Generic holder so any AndroidX ViewModel can live in an InstanceKeeper.
class VmHolder<T : ViewModel>(val vm: T) : InstanceKeeper.Instance {
    override fun onDestroy() { vm.clearForRetention() }  // calls protected onCleared via a thin shim
}
```

`AlbumDetailViewModel` keys on the album (matching `remember(album)` and the iOS
`.id(...)`): the `instanceKeeper` key becomes part of the `Config.Detail(album)`
identity, so pushing a different album yields a fresh VM and popping destroys the
old one.

**Lifecycle change vs today:** VMs now get **deterministic destruction** tied to
the component leaving the stack, rather than to composition disposal (Android) or
SwiftUI view identity (iOS). This is strictly more correct — e.g. popping
AlbumDetail will reliably cancel its scope on both platforms via one mechanism.
The logging conventions are preserved: keep each VM's `Logger.withTag("vm:…")`
and `init`/`onCleared` Debug logs (CLAUDE.md "What to log").

> **Constructor dependencies confirmed** (so `childFactory` can build them):
> `LibraryViewModel(libraryRepository, facade)`; `AlbumDetailViewModel(album, libraryRepository, facade, database)` (`AlbumDetailViewModel.kt:53`);
> `NowPlayingViewModel(facade, mcwsClient)`; `QueueViewModel(facade, libraryRepository)`;
> `ZonesViewModel(facade, libraryRepository)`; `SettingsViewModel(facade, database, clearPhysicalDownloads, isDebugBuild)`.
> All are available on the DI containers (`AppContainer.kt`, `AppContainer.swift`)
> — bundle them into an `AppDeps` passed to `RootComponent`.

---

## 5. Android host changes

`androidApp/.../MainActivity.kt`.

1. **Build the root component in the Activity** (see §7), then pass it into
   `MainShell`.
2. **Replace `when(shellState.activeTab)`** (`MainActivity.kt:301-373`) with the
   Compose-extensions `Children(...)`:

```kotlin
@Composable
fun MainShell(root: RootComponent, deps: AppDeps) {
    val stack by root.stack.subscribeAsState()      // from extensions-compose
    Scaffold(
        bottomBar = {
            // The custom premium tab bar stays EXACTLY as-is (MainActivity.kt:192-290).
            // Only the click handler and "selected" check change:
            //   selected = stack.active.configuration == config
            //   onClick  = { root.selectTab(config) }
            CustomTabBar(active = stack.active.configuration, onSelect = root::selectTab)
        },
    ) { padding ->
        Children(stack = stack, modifier = Modifier.padding(padding)) { child ->
            when (val c = child.instance) {
                is RootChild.Library  -> LibraryChildren(c.component)   // nested Children for List/Detail
                is RootChild.Server   -> ServerManagerScreen(facade = deps.facade, serverRepository = deps.serverRepository, onConnectSuccess = { /* handled in component */ })
                is RootChild.Player   -> PlayerChildren(c.component)    // nested Children for NowPlaying/Queue
                is RootChild.Zones    -> ZonesScreen(viewModel = c.component.vm, onBackClick = { root.selectTab(RootConfig.Player) })
                is RootChild.Settings -> SettingsScreen(viewModel = c.component.vm, onBackClick = { root.selectTab(RootConfig.Player) }, onDisconnectClick = root::onDisconnect)
            }
        }
    }
}
```

3. **Screens are reused unchanged.** `LibraryScreen(viewModel, onAlbumClick)`,
   `AlbumDetailScreen(viewModel, onBackClick)`, etc. keep their signatures; the
   callbacks now call component methods (`component.openAlbum`, `component.back`)
   instead of `viewModel.selectAlbum`.
4. **The custom tab bar** (`MainActivity.kt:192-290`) — animations, gold pill,
   mini-player row — is preserved verbatim. The `tabs` list (`MainActivity.kt:209`)
   maps to `RootConfig` values; `viewModel.selectTab(index)` becomes
   `root.selectTab(config)`.
5. **Mini-player, auto-connect scrim, toast** (`MainActivity.kt:135-160, 377-418`)
   stay; they read connect/toast state from the `RootComponent`'s plain
   `Value<ConnectUiState>` (or the retained `MainShellViewModel`, §12).

The `LibraryChildren`/`PlayerChildren` helpers are tiny nested `Children(...)`
composables driven by `LibraryComponent.stack` / `PlayerComponent.stack`.

---

## 6. iOS host changes (the main effort — be honest)

`iosApp/iosApp/ContentView.swift` and a new bridge file. **This is the hardest
part of the migration.** iOS has no `Children(...)`; we must observe Decompose's
`Value<ChildStack<…>>` from SwiftUI ourselves. The good news: the codebase already
has the exact pattern we mirror — `@Observable @MainActor` wrappers that do
`for await state in stateFlow` (e.g. `LibraryObservable` in `LibraryView.swift:6-64`,
`MainShellObservable` in `ContentView.swift:24-93`). We build the same shape over
`Value` instead of over a `StateFlow`.

### `Value`→SwiftUI observable bridge

Decompose's `Value<T>` exposes `value` plus `subscribe { }`/`unsubscribe { }`
(bridged by SKIE). Wrap it in an `@Observable` class consistent with the existing
observables:

```swift
// iosApp/iosApp/Core/ValueObservable.swift
import Observation
import SharedLogic

/// Bridges a Decompose `Value<T>` into a SwiftUI-observable property, mirroring
/// the `for await state in stateFlow` pattern used by LibraryObservable /
/// MainShellObservable. Holds the current value and refreshes on every emission.
@Observable
@MainActor
final class ValueObservable<T: AnyObject> {
    private(set) var value: T
    @ObservationIgnored private let source: Value<T>
    @ObservationIgnored private var cancellable: Cancellation?

    init(_ source: Value<T>) {
        self.source = source
        self.value = source.value
        // Decompose `subscribe` returns a Cancellation; SKIE exposes the observer
        // as an escaping closure. Mutating `value` triggers @Observable invalidation.
        self.cancellable = source.subscribe { [weak self] newValue in
            self?.value = newValue
        }
    }

    deinit { cancellable?.cancel() }
}
```

> Two SKIE/Decompose sharp edges to verify in Phase 4 (§12): (a) whether SKIE maps
> Decompose's `subscribe`/`Cancellation` cleanly or needs the
> `extensions-compose`-free `Value.observe(lifecycle) { }` form bridged through a
> Kotlin helper in `commonMain`; (b) generic `Value<ChildStack<C, T>>` may surface
> in Swift with erased generics — expose **concrete, non-generic accessor
> functions** from the components (e.g. `RootComponent.observeStack(onChange:)`)
> if the generic `Value` does not bridge well. Prefer adding thin
> `commonMain`/iOS-friendly accessors over fighting SKIE generics.

### Rewiring `ContentView`

- Replace `MainShellObservable` (`ContentView.swift:24-93`) with a
  `ValueObservable<RootChildStack>` (or a purpose-built `RootObservable` exposing
  `activeConfig` + the active child). The auto-connect/toast fields stay (driven
  by the retained connect state, §12).
- The `TabView` (`ContentView.swift:190-258`): its `selection` Binding's `get`
  reads the active `RootConfig` (mapped back to the int `.tag` if we keep the
  numeric tags, or switch tags to a `Hashable` config); its `set` calls
  `root.selectTab(config)`. **`UITabBarAppearance` styling (`ContentView.swift:152-171`)
  is unchanged.**
- `LibraryTabContainerView` (`ContentView.swift:393-422`): the
  `selectedAlbum == nil ? 1 : 0` opacity swap is replaced by observing
  `LibraryComponent.stack` — render `LibraryView` when the active child is `List`,
  `AlbumDetailView` when it's `Detail`. The `.id(...)` identity hack
  (`ContentView.swift:417`) is **no longer needed**: the component already keys the
  `AlbumDetailViewModel` per album via the config.
- `PlayerTabContainerView` (`ContentView.swift:379-391`): the `showQueue` bool
  becomes observation of `PlayerComponent.stack` (NowPlaying vs Queue child).
- Mini-player, auto-connect overlay, toast (`ContentView.swift:261-363`) stay;
  they read connect/toast state from the retained source (§12).

### SKIE & the sealed child types

SKIE bridges Kotlin `sealed interface` (`RootChild`, `LibraryComponent.Child`,
`PlayerComponent.Child`) as Swift enums you can `switch` over — same ergonomics
the app already relies on for sealed `…ContentState` types
(`AlbumDetailViewModel.kt:33`). The Swift host does
`switch root.activeChild { case .library(let c): … }`. Confirm in Phase 4 that
SKIE generates exhaustive Swift cases for these.

---

## 7. Wiring root creation

### Android (`MainActivity`)

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deps = AppDeps(appContainer, settings = sharedPrefsSettings())  // reuse MainActivity.kt:64-76
        // defaultComponentContext integrates savedStateRegistry (StateKeeper),
        // onBackPressedDispatcher (BackHandler), and lifecycle automatically.
        val root = RootComponent(defaultComponentContext(), deps)
        setContent {
            JrrTheme {
                CompositionLocalProvider(LocalMcwsClient provides deps.mcwsClient) {
                    MainShell(root = root, deps = deps)
                }
            }
        }
    }
}
```

`defaultComponentContext()` (from `decompose`) wires:
- **`StateKeeper`** ← Activity `savedStateRegistry` (process-death restore),
- **`BackHandler`** ← `onBackPressedDispatcher` (system back pops inner stacks),
- **`Lifecycle`** ← the Activity lifecycle.

The current `MainShellSettings` anonymous object (`MainActivity.kt:71-76`) is
reused as-is inside `AppDeps`.

### iOS (`AppDelegate` / `ContentView`)

Decompose has no `defaultComponentContext` on iOS; build the context manually
with Essenty primitives (this is standard Decompose-on-iOS):

```swift
// In AppContainer (Core/AppContainer.swift) or AppDelegate, after container init.
let lifecycle = LifecycleRegistryKt.LifecycleRegistry()
let stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)  // wire to NSCoder later for restore
let ctx = DefaultComponentContext(
    lifecycle: lifecycle,
    stateKeeper: stateKeeper,
    instanceKeeper: nil,
    backHandler: nil
)
let root = RootComponent(componentContext: ctx, deps: appDeps)
// Drive the lifecycle from app foreground/background:
LifecycleRegistryExtKt.resume(lifecycle)   // on didFinishLaunching
```

`RootComponent` is created **once** in `AppContainer`/`AppDelegate`
(`AppContainer.swift:28`, `AppDelegate.swift:23`) — *not* in `ContentView.init`
where today's VMs are built (`ContentView.swift:108`) — so it survives view
re-creation. `ContentView` receives it via `@Environment`/init like the container
already is (`iOSApp.swift:11`). Lifecycle is resumed on
`application(_:didFinishLaunchingWithOptions:)` (`AppDelegate.swift:11`) and can
be paused/stopped on background for correctness.

---

## 8. Process-death state restoration & back handling

### Free with Decompose (verify, don't assume)

- **Back stack restoration.** Because every `*Config` is `@Serializable` and
  `childStack(...)` is fed a serializer, Essenty's `StateKeeper` persists and
  restores the entire tab + sub-stack tree across process death — including the
  selected `Album` (`PlaybackModels.kt:165` is already `@Serializable`). Today's
  hand-rolled state has **no** restoration: `MainShellState` lives only in the
  VM's `MutableStateFlow` (`MainShellViewModel.kt:51`) and is lost on process
  death. This is a concrete upgrade.
- **System back handling.** `handleBackButton = true` on each `childStack` +
  `BackHandler` from `defaultComponentContext()` means Android system-back pops
  AlbumDetail→Library and Queue→NowPlaying automatically — currently there is no
  back handling on Android (`MainActivity.kt` has no `BackHandler`).

### Must verify

- **Android:** that `defaultComponentContext()` correctly bridges
  `savedStateRegistry` and that VM state held in `instanceKeeper` survives
  config-change (rotation) and is `clear()`-ed on real destroy.
- **iOS:** `StateKeeper` is **not** automatic. Process-death restore on iOS
  requires wiring `StateKeeperDispatcher` to `NSCoder`/scene state restoration.
  **Decision:** ship Phase 4 with in-session retention only (root held in
  `AppContainer`), and treat iOS state-restoration-across-cold-launch as an
  **optional follow-up** (§9) — iOS rarely process-kills foregrounded apps, so
  this is low-value relative to effort.
- The auto-connect "attempted once" guard (`MainShellViewModel.kt:71`,
  `hasAttemptedAutoConnect`) must remain idempotent across restore.

---

## 9. Optional follow-ups

- **Deep links.** With typed `RootConfig`/inner configs, a URL like
  `jrr://album/<folderPath>` can be parsed to push a `LibraryComponent.Config.Detail`.
  Not needed today; trivial to add later.
- **Predictive back (Android).** `decompose-extensions-compose` ships predictive-back
  animation for `Children(...)`; opt in once base migration is stable.
- **iOS cold-launch state restoration.** Wire `StateKeeperDispatcher` to scene
  state restoration (§8).
- **Tablet / multi-pane.** Decompose's `childSlot`/multiple `Value`s make a
  list-detail two-pane Library straightforward later; out of scope now.

---

## 10. Phased rollout

Each phase is independently buildable and committable. Verification commands per
phase (the repo builds Android via Gradle and iOS via `xcodebuild`):

| Phase | Verify (Android) | Verify (shared) | Verify (iOS) |
| --- | --- | --- | --- |
| any | `./gradlew :androidApp:assembleDebug` | `./gradlew :sharedLogic:testAndroidHostTest` | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator build` |

**Phase 0 — Compatibility spike (no commit if blocked).**
Confirm a Decompose/Essenty release supports Kotlin `2.3.21`. Add the deps to a
throwaway branch, build `:sharedLogic`. If no compatible tag exists, **stop and
report** — the rest of the plan is blocked.

**Phase 1 — Configs + `RootComponent` behind the scenes (shared only).**
Add `decompose` to `commonMain`, create `presentation/navigation/` with
`RootConfig`, `RootComponent`, `LibraryComponent`, `PlayerComponent`, the other
leaf components, `AppDeps`, and the `VmHolder`/`instanceKeeper` adapter. **Not
wired into any host yet.** Add a `commonTest` that drives `RootComponent.selectTab`
/ `openAlbum` and asserts stack contents (replaces the implicit logic in
`MainShellViewModel`). Verify: `:sharedLogic:testAndroidHostTest` green;
`:androidApp:assembleDebug` still builds (unused new code).

**Phase 2 — Migrate Android host.**
Add `decompose-extensions-compose` to `androidApp`. Rewrite `MainShell` to
`Children(...)` + nested children; build `RootComponent` in `MainActivity.onCreate`
via `defaultComponentContext()`. Screens unchanged; tab bar unchanged. Delete the
`when(activeTab)` and the `remember { … }` VM creation. Verify:
`:androidApp:assembleDebug`; manual smoke (tab switch keeps album selection;
system back pops detail/queue; rotation keeps stack).

**Phase 3 — iOS bridge groundwork.**
Add `ValueObservable` (and any concrete component accessors needed for SKIE). Build
`RootComponent` in `AppContainer`/`AppDelegate`. **Do not yet rewire `ContentView`'s
TabView** — verify the bridge compiles and emits by logging stack changes
(`ui:iOS:RootNav`). Verify: `xcodebuild`.

**Phase 4 — Migrate iOS host.**
Rewire `ContentView`'s `TabView`, `LibraryTabContainerView`, `PlayerTabContainerView`
to observe the component stacks; remove `MainShellObservable`'s navigation fields
(keep connect/toast). Remove the `.id(...)` hack. Verify: `xcodebuild`; manual
smoke parity with Android.

**Phase 5 — Cleanup.**
Delete now-dead navigation code from `MainShellViewModel` (keep auto-connect/toast,
relocated per §12), drop magic-int `selectTab(Int)`. Update `docs/`. Verify all
three commands.

Safest ordering rationale: shared component tree first (testable in isolation),
**Android before iOS** (Compose `Children` is turnkey; surfaces component-design
bugs cheaply), iOS last (hardest, benefits from a proven component tree).

---

## 11. File-by-file change list

### New files (shared)

- `sharedLogic/src/commonMain/kotlin/com/jrr/jrrkmp_native_ui/presentation/navigation/RootConfig.kt`
- `…/navigation/RootComponent.kt`
- `…/navigation/LibraryComponent.kt`
- `…/navigation/PlayerComponent.kt`
- `…/navigation/LeafComponents.kt` (`ServerComponent`, `ZonesComponent`, `SettingsComponent` — thin VM holders)
- `…/navigation/AppDeps.kt` (bundles facade, repos, mcwsClient, database, settings)
- `…/navigation/VmKeeper.kt` (`VmHolder` + `instanceKeeper` helpers)
- `sharedLogic/src/commonTest/kotlin/.../RootComponentTest.kt`

### New files (iOS)

- `iosApp/iosApp/Core/ValueObservable.swift` (the `Value`→SwiftUI bridge)
- `iosApp/iosApp/Core/RootObservable.swift` (optional concrete root wrapper)

### Edited files

- `gradle/libs.versions.toml` — add `decompose`, `essenty` versions + libraries (§2).
- `sharedLogic/build.gradle.kts` — `api(libs.decompose)` in `commonMain` (`:51`).
- `androidApp/build.gradle.kts` — `implementation(libs.decompose.extensions.compose)` (`:14`).
- `androidApp/.../MainActivity.kt` — build `RootComponent` in `onCreate`; rewrite
  `MainShell` to `Children(...)`; drop `when(activeTab)` and `remember`-based VM
  creation (`:79-92`, `:301-373`, `:105-133`).
- `iosApp/iosApp/Core/AppContainer.swift` or `Core/AppDelegate.swift` — construct
  `RootComponent` + Essenty context once (`AppContainer.swift:28`, `AppDelegate.swift:23`).
- `iosApp/iosApp/ContentView.swift` — observe component stacks; rewire `TabView`,
  `LibraryTabContainerView`, `PlayerTabContainerView`; remove `.id(...)` hack
  (`:190-258`, `:379-422`).
- `sharedLogic/.../viewmodel/MainShellViewModel.kt` — strip navigation
  (`selectTab/selectAlbum/setShowQueue/activeTab/selectedAlbum/showQueue`), **retain
  auto-connect + toast** (relocate into `RootComponent` or keep as a slim
  `ConnectViewModel`). See §12.

### Untouched (intentionally)

All `presentation/screens/*` (Android), all `Presentation/Views/*` (iOS), all
feature ViewModels' bodies, `AudioPlayerFacade`, repositories, `McwsClient`, both
DI `AppContainer`s, the logging stack (`core/logging/*`).

---

## 12. Risks & mitigations

| Risk | Mitigation |
| --- | --- |
| **Decompose may not yet support Kotlin 2.3.21** (`libs.versions.toml:14`). | Phase 0 gate. If blocked, stop and report; do not downgrade Kotlin. |
| **SKIE × Decompose generics.** `Value<ChildStack<C, T>>` may bridge to Swift with erased/awkward generics. | Expose concrete, non-generic accessor functions (`observeStack(onChange:)`, `activeChild`) from components in Kotlin; bridge those instead of raw generic `Value`. Validate in Phase 3 before committing the iOS host rewrite. |
| **`subscribe`/`Cancellation` bridging.** SKIE may not expose `Value.subscribe` ergonomically. | Provide a `commonMain` helper that adapts `Value` to a `StateFlow` (the app already bridges `StateFlow` cleanly — `LibraryObservable`, `ContentView.swift:46`), then reuse the existing `for await` pattern. Lowest-risk fallback. |
| **`instanceKeeper` vs `remember`/`@State` VM creation.** Lifecycle semantics differ; double-creation or leaked scopes possible. | Centralise in `VmHolder`/`getOrCreate` with a single `onDestroy → clear()` path; assert in `commonTest` that popping a child destroys its VM. |
| **Auto-connect logic must be preserved** (`MainShellViewModel.kt:70-155`). It's the trickiest existing behaviour (idempotent guard, Offline shortcut, cancel, toast). | Move it verbatim into `RootComponent` (using `coroutineScope()`), or keep a slim `ConnectViewModel`; only its navigation side-effect (`activeTab = 2`) becomes `selectTab(RootConfig.Player)`. Cover with the existing manual smoke + a unit test on the guard. |
| **iOS process-death restore is not free** (§8). | Scope it out of the base migration; document as follow-up. |
| **Tab-tap-to-pop-root idiom** (`MainShellViewModel.kt:165-185`) is subtle. | Re-implement explicitly in `RootComponent.selectTab` + `LibraryComponent.popToRoot`; assert in test. |
| **Logging conventions.** | New components use `Logger.withTag("vm:RootNav")` etc. (no `jrr:` prefix — CLAUDE.md), lambda form `log.d { … }`, log `init`/`onDestroy` and `selectTab`/`openAlbum` at Debug, errors with `Throwable`. iOS bridge uses `SwiftLog("ui:iOS:RootNav")`. |
| **Commit hygiene.** | Conventional Commits, **no AI attribution** (CLAUDE.md / global rules). One commit per phase, e.g. `feat(navigation): add Decompose RootComponent and tab configs`. |

---

## 13. Effort estimate & recommendation

| Phase | Scope | Estimate |
| --- | --- | --- |
| 0 — Compatibility spike | Verify Kotlin 2.3 support, add deps | 0.5 day |
| 1 — Configs + components (shared) + tests | `RootComponent`, 2 inner stacks, leaf components, VM keeper, unit tests | 2 days |
| 2 — Android host | `Children(...)`, root creation, drop `when(activeTab)` | 1–1.5 days |
| 3 — iOS bridge groundwork | `ValueObservable`, accessors, root in AppContainer, SKIE validation | 1.5–2 days |
| 4 — iOS host rewrite | `TabView`/container rewire, remove `.id` hack | 2 days |
| 5 — Cleanup | strip dead nav, relocate auto-connect, docs | 0.5–1 day |
| **Total** | | **≈ 7.5–9 days** |

### Go / no-go

**Qualified GO — but only partial Decompose, and only if Phase 0 clears.**

The app's navigation is **shallow**: 5 tabs, two one-level sub-stacks, no deep
links, no multi-pane. The hand-rolled model in `MainShellViewModel` works today.
The *concrete* wins from Decompose are (1) typed routes replacing magic ints,
(2) real back stacks with free Android system-back and process-death restoration,
(3) a single shared navigation source of truth with deterministic VM lifecycles.
Those are real but modest for an app this size.

The *cost* is concentrated almost entirely in the **iOS `Value`→SwiftUI bridge**
(Phases 3–4, ~half the effort), and carries genuine SKIE-generics risk.

**Recommendation:** proceed **only if** Phase 0 confirms Kotlin-2.3 compatibility
**and** the team values process-death restore / typed routing / a future
deep-link or tablet story. If the goal is purely cosmetic (kill the magic ints),
a far cheaper non-Decompose refactor — replace `activeTab: Int` with a
`sealed interface Tab` and `selectedAlbum`/`showQueue` with small sealed
sub-route types inside the existing `MainShellViewModel` — captures ~60% of the
benefit at ~15% of the cost and zero new dependencies. Pick full Decompose when
the navigation graph is expected to grow; otherwise prefer the lightweight
sealed-route refactor.
