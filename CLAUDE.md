# Project conventions

This file documents conventions that aren't obvious from looking at the code.
Apply them when adding new code and when modifying existing code.

## Logging

The project uses [Kermit 2.1+](https://kermit.touchlab.co/) for structured
logging across the shared module and both platforms. Bootstrap happens once
per platform in `JrrApplication.onCreate()` (Android) and
`AppDelegate.application(_:didFinishLaunchingWithOptions:)` (iOS) — see
`AppLogger.configure(...)` in `core/logging/AppLogger.kt`.

### Severity convention

| Level     | Purpose                                                            | Volume budget                  |
| --------- | ------------------------------------------------------------------ | ------------------------------ |
| `Verbose` | High-volume internals — poll ticks, per-frame state, SQL queries   | Dev only (filtered in release) |
| `Debug`   | State transitions, lifecycle (`init`, `dispose`), user actions     | Dev default                    |
| `Info`    | Events you'd want in a bug report — connect, zone switch, downloads | Release default                |
| `Warn`    | Recoverable problems — fallback, retry, missing optional data       | Always on                      |
| `Error`   | Caught exceptions, failed operations. Always pass the `Throwable`. | Always on                      |
| `Assert`  | Programmer-error contract violations                                | Always on                      |

Release builds default to `Info`. Dev builds default to `Verbose`. Users can
override the minimum severity at runtime via Settings → debug controls.

### Tag taxonomy

Tags are two-segment: `<subsystem>:<concrete>`. Use these prefixes:

- `vm:*` — view models (`vm:AlbumDetail`, `vm:Library`, …)
- `net:*` — network (`net:Mcws`, `net:Ktor`)
- `repo:*` — repositories (`repo:Library`, `repo:Server`)
- `playback:*` — playback (`playback:Facade`, `playback:Local`, `playback:Remote`)
- `db:*` — database (`db:Room`, `db:DownloadJob`, …)
- `di:*` — dependency injection (`di:AppContainer`)
- `ui:<platform>:*` — UI layer (`ui:Android:AlbumDetail`, `ui:iOS:AlbumDetail`)
- `lifecycle:*` — app/VM lifecycle hooks

Cache the logger as a class member at the top of every class that logs:

```kotlin
class AlbumDetailViewModel(...) : ViewModel() {
    private val log = Logger.withTag("vm:AlbumDetail")
    // …
}
```

### Always use the lambda form

```kotlin
log.d { "loaded ${tracks.size} tracks" }   // good — lazy
log.d("loaded ${tracks.size} tracks")      // bad — always builds the string
```

The lambda is never invoked when the severity is below threshold, so it's
free in release builds.

### What to log

- **Every `init` and `dispose`/`onCleared`** of a VM at Debug.
- **Every public method entry** on VMs, repos, and the playback facade at Debug.
  Include relevant arg summaries (`fileKey`, `track.name`, etc.).
- **Every caught exception** at Error, with the `Throwable` attached:
  `log.e(e) { "loadTracks failed" }`. Never `e.printStackTrace()`.
- **Every transient error** set on a `*ViewState` at Warn.
- **Network requests/responses** at Debug (via Ktor `Logging` plugin → Kermit
  bridge in `McwsCore`).
- **State transitions** via the `.logged(log, "name") { it.summary() }`
  Flow operator (see `LogExtensions.kt`).

### What NOT to log

- **Auth tokens, passwords, raw credentials** — use `String?.redact()` from
  `LogExtensions.kt`. Tokens, API keys, anything bearer-shaped.
- **Full Room entity dumps** in Verbose loops — they're huge. Summarise:
  `tracks.size` or `tracks.take(3).map { it.name }`.
- **Cancellation exceptions** — they're normal coroutine teardown. The
  `.logged()` operator and `runCatchingLogged` both filter them already.

### Cross-cutting helpers (in `core/logging/LogExtensions.kt`)

- `String?.redact()` — safely log token-shaped values
- `Logger.runCatchingLogged(op) { … }` — try/catch with structured logging
- `Flow<T>.logged(log, name) { summary }` — instrument flow lifecycle

### iOS

The Swift app calls Kermit through SKIE (`Logger.companion.withTag(...)` ).
Same severity/tag conventions apply. Native `OSLog` is not used — keep
everything in one pipeline so the in-app "Share debug log" feature surfaces
all events.

### Debug log export

`AppLogger.recentLogs()` returns a snapshot of the last 1000 log lines from
the in-memory ring buffer. This powers the **Share debug log** action on
the Settings screen so users can attach recent activity to bug reports —
on Android via `Intent.ACTION_SEND`, on iOS via SwiftUI `ShareLink`.

### Runtime severity selector

`SettingsViewModel.setLogSeverity(severity)` calls `AppLogger.setMinSeverity`,
adjusting the Kermit floor for the running process. The Settings → Logging
section exposes a V/D/I/W/E button row, gated on `state.isDebugBuild`, so
debug builds can flip the floor at runtime without a rebuild. `isDebugBuild`
is passed into `SettingsViewModel` from each platform's AppContainer
(Android: `ApplicationInfo.FLAG_DEBUGGABLE`; iOS: `#if DEBUG`).

## Commit style

- Conventional Commits: `feat|fix|chore|docs|refactor|test(<scope>): <description>`
- Scopes: `feat(auth)`, `fix(home_screen)`, `refactor(api_client)`, etc.
- No AI attribution trailers on commits.
