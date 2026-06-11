# Plan: Arrow adoption in JRR

Adopting [Arrow](https://arrow-kt.io) (v2.2.2) in the shared KMP module.
Arrow publishes artifacts for every target `sharedLogic` builds (android,
jvm, iosArm64, iosSimulatorArm64, macosArm64, tvosArm64, tvosSimulatorArm64),
so there is no target friction — adoption is a design question, not a build
question.

**Status:** Phases 1–3 implemented (branches `arrow-phase1-typed-errors`,
`arrow-phase2-resilience`, `arrow-phase3-parallel-loads`, stacked in that
order). Phase 4 (optics) remains deferred.

**Version note:** pinned to 2.2.2, not 2.2.3 — the 2.2.3 *native* klibs are
built with the Kotlin 2.4.0 compiler (klib ABI 2.4.0), which our Kotlin
2.3.21 toolchain cannot consume. JVM/Android artifacts don't care, so a
plain `./gradlew check` passes on 2.2.3 and the breakage only shows up when
linking an Apple framework. Bump Arrow together with the Kotlin upgrade.

## Ground rules (apply to every phase)

1. **Arrow stays inside `sharedLogic`.** Nothing SKIE-facing (ViewModels'
   public methods, facade API consumed by Swift) exposes `Either`/`Raise` —
   Kotlin generics erase at the ObjC bridge, so Swift would see
   `Either<AnyObject, AnyObject>`. ViewModels unwrap to what they already
   use: `*ViewState` fields, nullables, or thrown exceptions (SKIE →
   `async throws`).
2. **One phase = one branch = one PR**, each passing the strict pre-push
   gate (`./gradlew check -PstrictWarnings` + warnings-as-errors iOS build).
3. Dependencies land in `libs.versions.toml` (`arrow = "2.2.3"`); only the
   modules a phase needs get added.

## Phase 1 — Typed errors in the connection flow (`arrow-core`)

**Problem (real code):** `ServerRepository.authenticate(...)`
(ServerRepository.kt:106) returns `String?` — four distinct failures
collapse into one `null`, so `ServerManagerScreen` can only say
"connection failed", never "wrong password" vs "server unreachable":

```kotlin
// today
suspend fun authenticate(...): String? // null = HTTP error? bad creds? timeout?
```

**After:** a small error ADT + `Either`:

```kotlin
sealed interface McwsError {
    data class Unreachable(val host: String, val cause: Throwable) : McwsError
    data class HttpError(val code: Int) : McwsError
    data class McwsRejected(val status: String) : McwsError   // bad credentials land here
    data class ParseError(val cause: Throwable) : McwsError
}

suspend fun authenticate(...): Either<McwsError, String> = either {
    val response = catch({ httpClient.get(url) { ... } }) { e: Exception ->
        if (e is CancellationException) throw e
        raise(McwsError.Unreachable(host, e))
    }
    ensure(response.status.value in 200..299) { McwsError.HttpError(response.status.value) }
    val xml = parseMcwsResponse(response.bodyAsText())
    ensure(xml.status == "OK") { McwsError.McwsRejected(xml.status) }
    ensureNotNull(xml.items["Token"]) { McwsError.ParseError(IllegalStateException("no Token")) }
}
```

**Boundary unwrap** (e.g. `MainShellViewModel` / `TvConnectViewModel`):

```kotlin
repository.authenticate(...).fold(
    ifLeft = { err -> state.update { it.copy(error = err.toUserMessage()) } },
    // "Wrong access key" vs "Server unreachable"
    ifRight = { token -> connect(token) },
)
```

**Scope:** `authenticate`, `checkAlive`, `lookupAccessKey` (currently
`WebPlayLookupResult?` — same null-collapse problem; its ATS failure was
debugged blind for exactly this reason). ~3 functions + their VM call sites.

## Phase 2 — Retry/backoff with `Schedule` (`arrow-resilience`)

**Problem (real code):** `AudioPlayerFacade.startPolling()` is a hand-rolled
`while(isActive) { try … catch { log.w }; delay(1000) }` — a down server
gets hammered every second forever, each failure logging a warning.
`recoverActiveServer` has no retry at all: one flaky-WiFi moment at app
start and you stay disconnected.

**After:**

```kotlin
// polling: fixed cadence while healthy, exponential backoff (capped 30s) while failing
private val pollSchedule = Schedule.exponential<Throwable>(1.seconds)
    .doUntil { _, duration -> duration > 30.seconds }
    .andThen(Schedule.spaced(30.seconds))

// recovery: 5 attempts with jittered backoff instead of give-up-on-first-failure
Schedule.exponential<Throwable>(500.milliseconds).jittered().take(5)
    .retry { serverRepository.recoverActiveServer(...) }
```

**Scope:** `startPolling`, the `recoverActiveServer` call in the facade
`init`, possibly `DownloadWorker`'s OkHttp call.
**Behavior change to flag in review:** poll cadence degrades under failure
(today: constant 1s).

## Phase 3 — Parallel loads (`arrow-fx-coroutines`)

**Problem (real code):** screens load sequentially what is independent —
e.g. the TV library screen awaits `artists()` then `randomAlbums()`;
`AlbumDetailViewModel` loads tracks, then favorite state.

**After:**

```kotlin
val (artists, albums) = parZip(
    { libraryRepository.getArtists() },
    { libraryRepository.getRandomAlbums(24) },
) { a, b -> a to b }
```

Failure semantics improve for free: if one leg throws, the other is
cancelled (structured concurrency), instead of half-loaded states.
**Scope:** 3–4 screen loaders on the TV/CarPlay startup paths. No API
changes.

## Phase 4 (optional, propose separately) — `arrow-optics` for ViewState copies

Generated lenses replace nested
`state.copy(playback = state.playback.copy(...))` chains. Costs a KSP
plugin on `sharedLogic` (KSP wiring already exists for Room) and
annotations on state classes. Hold until Phases 1–3 prove the library
earns its place.

## What we deliberately do NOT do

- No `Either` in SKIE-facing signatures (rule 1).
- No `Option` — Kotlin nullables are better here; `Option` would fight the
  existing style.
- No big-bang conversion of `LibraryRepository`'s ~20 functions; its
  try/catch-with-offline-fallback pattern works and can migrate
  opportunistically later.

## Verification per phase

- `./gradlew check -PstrictWarnings` + strict iOS build (the pre-push gate
  covers it)
- Phase 1 adds unit tests asserting *which* `McwsError` each failure yields
  (wrong creds vs unreachable — testable with the existing repo test
  patterns)
- Phase 2: manual check against the real server — pull the network cable
  mid-poll, watch `jrr:playback:Facade` logs back off

## Order & size

| Phase | Modules added | ~Touch | Risk |
|---|---|---|---|
| 1. Typed errors (connect flow) | `arrow-core` | ~6 files | Low — additive, leaf functions |
| 2. Resilience (polling/recovery) | `arrow-resilience` | 2 files | Medium — timing behavior changes |
| 3. Parallel loads | `arrow-fx-coroutines` | 3–4 files | Low |
| 4. Optics | `arrow-optics` + KSP | wide but mechanical | Defer |

## Review decisions (2026-06-11)

1. **Boundary rule** — Arrow stays out of the SKIE surface. Implementation:
   the Either-returning `ServerRepository` members (`authenticateTyped`,
   `checkAliveTyped`, `lookupAccessKeyTyped`) are `@HiddenFromObjC`; Swift
   keeps the original nullable wrappers. `McwsError` itself is a plain
   sealed interface and *is* exported, so Swift could branch on it later.
2. **Phase 2 backoff** — approved. Polling cadence degrades while the
   server fails (1s → exponential → steady 30s probe) and resets on the
   first successful poll.
3. **Approval scope** — all of Phases 1–3, one branch/PR per phase.

## Implementation notes (what differed from the draft)

- `ServerRepository` is called directly from Swift (TvConnectView,
  TvRootView, ServerManagerView), so the nullable functions stayed as the
  SKIE-facing API and the typed variants were added alongside, rather than
  replacing them.
- `McwsClient.getPlaybackInfo` returns null on *all* failures (transport
  errors are swallowed in `getRaw`), so the poll loop converts null into a
  `PollUnavailableException` to drive the backoff `Schedule`.
- Phase 3 shrank to one call site: `AlbumDetailViewModel` (network tracks
  ∥ DB favorite). The TV screens the draft flagged already load via
  separate `LaunchedEffect`s (concurrent), and `getBrowseNode`'s
  children-then-files sequence is deliberate — `Browse/Files` on a
  non-leaf node could pull an entire subtree, so it must stay the
  fallback, not a parallel prefetch.
