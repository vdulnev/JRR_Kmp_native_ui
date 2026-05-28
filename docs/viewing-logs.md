# Viewing logs in the terminal

The app routes every log line through Kermit → `platformLogWriter()`, which
maps onto:

- **Android**: Android's Logcat (`adb logcat`)
- **iOS**: Apple's unified logging system (`os_log`), readable via the
  `log` CLI, `xcrun simctl`, `idevicesyslog`, or Console.app

Every log line is tagged `jrr:<subsystem>:<concrete>` (the `jrr:` prefix is
applied automatically at the writer layer — see `PrefixingLogWriter.kt`),
so a single `grep 'jrr:'` cuts through system noise on either platform.

## Android — `adb logcat`

The standard tool. The `jrr:` prefix makes filtering trivial:

```bash
# All app logs, live
adb logcat | grep 'jrr:'

# Just network
adb logcat | grep 'jrr:net:'

# One specific VM
adb logcat | grep 'jrr:vm:AlbumDetail'

# Errors only, app only
adb logcat *:E | grep 'jrr:'

# Tail history (last few hundred lines) instead of streaming
adb logcat -d | grep 'jrr:' | tail -n 200
```

Recommended `~/.zshrc` alias:

```bash
alias jrrlog-android='adb logcat | grep "jrr:"'
```

## iOS Simulator

Apple's `log` CLI runs inside the booted simulator via `xcrun simctl spawn`.
It treats the simulator like a remote host whose logs you're reading.

**Two gotchas to know up front:**

1. **`log show` filters out Info/Debug by default** — without `--info --debug`,
   you only see Default/Error/Fault, which means almost every Kermit line is
   suppressed. Always pass both flags when investigating.
2. **Kermit's iOS writer uses `NSLog`**, not custom-subsystem `os_log`. That
   means `subsystem` is the system default (not your bundle ID) and `category`
   is empty. Filter by `process` (the binary name) or `eventMessage` instead.

```bash
# All app logs, live (note --info --debug + process filter)
xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui \
    2>/dev/null | grep 'jrr:'

# Tail history — same predicate + the --info --debug flags
xcrun simctl spawn booted log show --last 5m --info --debug \
    --predicate 'process == "JRRKmpnativeui"' \
    2>/dev/null | grep 'jrr:'

# Broadest possible match — regex on the message text
xcrun simctl spawn booted log stream --info --debug \
    --predicate 'eventMessage CONTAINS "jrr:"' 2>/dev/null

# Just network — combine the process filter with grep
xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui \
    2>/dev/null | grep 'jrr:net:'

# Errors only (severity filter via the level flag — works regardless of NSLog)
xcrun simctl spawn booted log stream --process JRRKmpnativeui --level error \
    2>/dev/null | grep 'jrr:'
```

The `2>/dev/null` swallows the benign `getpwuid_r did not find a match for uid 501`
warning from the spawned `log` process — it doesn't affect output, just clutters
the terminal.

### How Kermit maps onto Apple's predicate keys

Kermit's `platformLogWriter()` on iOS / iOS Simulator routes through
`NSLog`. NSLog writes to the unified logging system but with these
characteristics:

| Predicate key | What Kermit gives you |
|---|---|
| `process` | The binary name (`JRRKmpnativeui`) — **most reliable filter** |
| `eventMessage` | The rendered log message, including `jrr:<tag>` prefix |
| `subsystem` | System default (`com.apple.console` or empty) — **do NOT use this for filtering** |
| `category` | Empty — **do NOT use this** |
| `messageType` / `--level` | `default` for everything Info-and-above, `debug` for Debug, `error` for Error |

`--predicate` syntax is [NSPredicate](https://developer.apple.com/documentation/foundation/nspredicate):
operators include `==`, `!=`, `CONTAINS`, `BEGINSWITH`, `ENDSWITH`,
`MATCHES` (regex), combined with `AND` / `OR` / `NOT`.

### Useful filter combinations

```bash
# Drop the chatty SQL stream
xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui \
    2>/dev/null | grep 'jrr:' | grep -v 'jrr:db:SQL'

# Only VM logs
xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui \
    2>/dev/null | grep 'jrr:vm:'

# Combine process + message substring at the predicate level
xcrun simctl spawn booted log stream --info --debug \
    --predicate 'process == "JRRKmpnativeui" AND eventMessage CONTAINS "failed"' \
    2>/dev/null
```

### Simulator must be booted

```bash
# Check
xcrun simctl list devices booted

# Boot one if none
xcrun simctl boot "iPhone 16 Pro"
open -a Simulator
```

## iOS Physical Device

Two paths.

### Apple's `log` CLI (Xcode required)

Find the device's UDID, then stream:

```bash
# List connected devices
xcrun xctrace list devices

# Stream from device by UDID
log stream --device <UDID> \
    --predicate 'subsystem == "com.jrr.jrrkmp-native-ui.JRRKmpnativeui"'
```

### `idevicesyslog` (no Xcode required)

Lighter-weight, comes from libimobiledevice:

```bash
brew install libimobiledevice
idevicesyslog --process JRRKmpnativeui | grep 'jrr:'
```

## Console.app (GUI)

For interactive filtering with autocomplete and saved filters, Apple's
**Console.app** (in `/System/Applications/Utilities/`) attaches to
simulators and physical devices. Useful saved filter:

```
category contains "jrr:"
```

Save it as a preset so you can switch in one click. Roughly equivalent
to Android Studio's Logcat panel.

## Xcode's debug console

When you launch the app from Xcode, the bottom debug console shows the
same `os_log` stream that `log stream` would. No need to open a separate
terminal — same data, same predicate filtering via the search bar.

## Controlling message format and wrapping

Three independent layers shape what you see:

### 1. Apple's `log` output style — `--style`

The default style draws a wide table with thread/type/activity/PID/TTL
columns. Long messages then look chaotic because of column padding.
Switch to `compact` for grep workflows:

```bash
# Default — multi-column table
xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui \
    2>/dev/null | grep 'jrr:'

# Compact — timestamp + level + message, single line
xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui \
    --style compact 2>/dev/null | grep 'jrr:'
```

| `--style` | When to use |
|---|---|
| `default` | When you need thread/PID/activity columns visible — debugging concurrency |
| `compact` | Day-to-day tailing — narrowest format that still has the message |
| `syslog` | Pipe into existing syslog-aware tools |
| `json` / `ndjson` | Programmatic processing via `jq` — also bypasses the 1024-char truncation |

### 2. Terminal wrapping — your shell, not Apple's `log`

Long single lines wrap when the terminal emulator hits the right edge.
Three options:

**Horizontal scroll instead of wrap** (best for tailing):

```bash
... | grep 'jrr:' | less -S +F
```

`less -S` disables wrap (scroll with `→`), `+F` makes `less` follow the
stream like `tail -f`. Use `Ctrl-C` then `q` to exit.

**Hard truncate to fixed width**:

```bash
... | grep 'jrr:' | cut -c 1-200
```

**Soft-wrap at word boundaries**:

```bash
... | grep 'jrr:' | fold -s -w 120
```

### 3. Long-message truncation — `--style json` to bypass

Apple's `log` truncates `eventMessage` to ~1024 chars and appends `…`.
There's no flag to lift the cap directly, but the JSON output gives you
the full message:

```bash
xcrun simctl spawn booted log show --last 5m --info --debug \
    --process JRRKmpnativeui --style json 2>/dev/null \
    | jq -r 'select(.eventMessage | contains("jrr:")) | .eventMessage'
```

`jq -r` drops the surrounding quotes so multi-line messages render
naturally. This is also the cleanest format for grep-replace pipelines.

### 4. Keep messages compact at the source

Kermit doesn't truncate — whatever your lambda returns goes through
verbatim. So the on-the-wire size is up to you:

- **Use the `.summary()` extension on every `*ViewState`** — they're
  designed to compress state into a one-liner. Already wired in every VM.
- **Avoid `log.v { tracks.toString() }`-style dumps.** Truncate at the
  call site: `tracks.take(3).map { it.name }`.
- **Avoid logging full HTTP bodies.** `LogLevel.HEADERS` in
  `KtorLogBridge`'s install block already omits them.

## Color and clickable URLs

The app logs full MCWS URLs (with `Token=xxxx` redacted). Being able to
click them straight from the terminal — to fire the same request in a
browser and see the raw server response — is invaluable for debugging.

### Most modern terminals do this for you

URL detection is a rendering-layer feature, not a CLI feature. If you're
on any recent terminal app, URLs are auto-underlined and clickable with
no extra setup. Hover with the mouse — if the URL gets a line under it,
you're set. **`Cmd+Click`** (macOS) / **`Ctrl+Click`** (Linux) opens it
in your default browser.

| Terminal | URL detection | How to click |
|---|---|---|
| macOS Terminal.app | built-in | `Cmd+Click` |
| iTerm2 | built-in + Smart Selection rules | `Cmd+Click` |
| Warp | built-in, very polished | click directly |
| kitty | `hyperlink_hint` keymap | configurable |
| Alacritty (0.13+) | `hints` config | configurable |
| VS Code integrated terminal | built-in | `Cmd+Click` |
| Ghostty | built-in | `Cmd+Click` |

The earlier `***` → `xxxx` redaction marker fix matters here: triple
asterisks were breaking URL pattern matchers, so the URL was only
partially detected. With `xxxx` the URL stays intact through the
terminal's regex and the whole thing is clickable.

### Explicit color highlighting

If you want URLs to *visually pop* even before hovering, pipe through a
colorizer.

**Grep trick** — color URLs while keeping all other text:

```bash
jrrlog | grep --line-buffered --color=always -E 'https?://[^ ]+|$'
```

The `|$` alternation makes every line "match" so nothing is filtered out;
only the URL portion gets `grep`'s match-color. Customize via the
`GREP_COLORS` env var (e.g. `export GREP_COLORS='ms=36;4'` for cyan +
underline; `36` = cyan, `4` = underline ANSI codes).

**Sed for full control** — explicit ANSI escape wrapping:

```bash
jrrlog | sed -E $'s|https?://[^ ]+|\\x1b[36;4m&\\x1b[0m|g'
```

Wraps every URL in cyan-underlined ANSI and resets. Drop into an alias:

```bash
alias hilite='sed -E $\'s|https?://[^ ]+|\\x1b[36;4m&\\x1b[0m|g\''
alias jrrlogh='jrrlog | hilite'
```

### Purpose-built log viewer — `lnav`

For larger investigation sessions where you want filtering, color-coded
severity, AND URL highlighting all in one tool:

```bash
brew install lnav

# Capture a window of logs, then explore in lnav
xcrun simctl spawn booted log show --last 1h --info --debug \
    --process JRRKmpnativeui --style compact 2>/dev/null > /tmp/jrr.log
lnav /tmp/jrr.log
```

`lnav` auto-detects timestamps, severity, IPs, URLs; supports SQL queries
over the captured log; has tab-complete filtering. Overkill for live
`tail -f`, but unmatched for "I have hours of logs from a flake, find
the thing."

## Recommended aliases

Drop these in `~/.zshrc` for the iOS equivalent of `adb logcat | grep jrr:`.
The `--info --debug` flags and `process ==` filter are non-negotiable —
Kermit's NSLog-backed writer doesn't surface a custom subsystem / category,
and `log` hides Info/Debug by default.

```bash
# All app logs, live — compact format, line-buffered grep
alias jrrlog='xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui --style compact 2>/dev/null | grep --line-buffered "jrr:"'

# Same but with horizontal scroll instead of wrap (best for long lines)
alias jrrlogs='xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui --style compact 2>/dev/null | grep --line-buffered "jrr:" | less -S +F'

# Just network
alias jrrnet='xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui --style compact 2>/dev/null | grep --line-buffered "jrr:net:"'

# Just errors
alias jrrerr='xcrun simctl spawn booted log stream --process JRRKmpnativeui --level error --style compact 2>/dev/null | grep --line-buffered "jrr:"'

# Last 5 minutes from history (great after reproducing a bug)
alias jrrtail='xcrun simctl spawn booted log show --last 5m --info --debug --process JRRKmpnativeui --style compact 2>/dev/null | grep "jrr:"'

# Android side, for symmetry
alias jrrlog-android='adb logcat | grep "jrr:"'
```

Notes on the flag soup:

- **`--style compact`** — single-line format; the major readability win
- **`--line-buffered`** (grep) — flush per line instead of in 4 KB chunks,
  so live tailing doesn't feel bursty
- **`--info --debug`** — `log` hides these levels by default, suppressing
  almost every Kermit line
- **`2>/dev/null`** — swallows the benign `getpwuid_r` warning
- **`less -S +F`** — disables wrap and follows the stream (Ctrl-C then `q` to exit)

Show multiline logs
```bash
xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui --style compact 2>/dev/null | grep --line-buffered -A15 "jrr:" | awk '/^[0-9]{4}-[0-9]{2}-[0-9]{2}/ { if ($0 ~ /jrr:/) print_block = 1; else print_block = 0 } print_block'
```

## Inside the app — share-log button

Both platforms have a **SHARE LOG** button under Settings → Logging.
It exports the in-memory ring buffer (last 1000 lines) via the OS share
sheet, so users reporting bugs can attach the recent activity without
needing a terminal.

Debug builds also have a **V/D/I/W/E** severity selector right next to
the button so testers can temporarily flip the minimum-severity floor
at runtime — useful when reproducing a flake.

## Privacy redaction quirk (iOS only)

Apple's `os_log` redacts dynamic strings as `<private>` by default in
release mode. Kermit's `platformLogWriter` handles this so messages
render as plain text, but if you ever see `<private>` in `log stream`
output:

```bash
# Authorize plaintext logging on the simulator (until next boot)
sudo log config --mode "private_data:on"
```

This isn't a problem in practice for this app — Kermit emits messages
in a public-safe way — but it's the gotcha to know if you ever see
mysterious redactions.

## TL;DR symmetry

| Goal | Android | iOS Simulator |
|---|---|---|
| All app logs | `adb logcat \| grep 'jrr:'` | `xcrun simctl spawn booted log stream --info --debug --process JRRKmpnativeui 2>/dev/null \| grep 'jrr:'` |
| Network only | `adb logcat \| grep 'jrr:net:'` | (same as above) `... \| grep 'jrr:net:'` |
| Errors only | `adb logcat *:E \| grep 'jrr:'` | `xcrun simctl spawn booted log stream --process JRRKmpnativeui --level error 2>/dev/null \| grep 'jrr:'` |
| Tail last N min | `adb logcat -t '5 minutes ago' \| grep 'jrr:'` | `xcrun simctl spawn booted log show --last 5m --info --debug --predicate 'process == "JRRKmpnativeui"' 2>/dev/null \| grep 'jrr:'` |
