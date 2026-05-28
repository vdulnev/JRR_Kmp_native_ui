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
It treats the simulator like a remote host whose logs you're reading:

```bash
# All app logs, live
xcrun simctl spawn booted log stream --process JRRKmpnativeui | grep 'jrr:'

# Just network
xcrun simctl spawn booted log stream --predicate 'category BEGINSWITH "jrr:net:"'

# One specific category (Kermit tag → OSLog category)
xcrun simctl spawn booted log stream --predicate 'category == "jrr:vm:AlbumDetail"'

# Errors only, app only
xcrun simctl spawn booted log stream --process JRRKmpnativeui --level error | grep 'jrr:'

# Tail the last N minutes from history (great after reproducing a bug)
xcrun simctl spawn booted log show --last 5m \
    --predicate 'subsystem == "com.jrr.jrrkmp-native-ui.JRRKmpnativeui"'
```

### How Kermit maps onto Apple's predicate keys

| Predicate key | Source |
|---|---|
| `subsystem` | App's bundle ID (`com.jrr.jrrkmp-native-ui.JRRKmpnativeui`) |
| `category` | The Kermit tag (e.g. `jrr:vm:AlbumDetail`, `jrr:net:Ktor`) |
| `eventMessage` | The rendered log message body |
| `messageType` / `--level` | Maps roughly to severity (`info`, `debug`, `error`, `fault`) |

### Useful predicate combinations

```bash
# Everything in the net:* family but not the chatty db:SQL stream
xcrun simctl spawn booted log stream \
    --predicate 'category BEGINSWITH "jrr:" AND category != "jrr:db:SQL"'

# Only VM logs across the app
xcrun simctl spawn booted log stream \
    --predicate 'category BEGINSWITH "jrr:vm:"'

# Combine subsystem + level + message substring
xcrun simctl spawn booted log stream \
    --predicate 'subsystem == "com.jrr.jrrkmp-native-ui.JRRKmpnativeui" AND eventMessage CONTAINS "failed"'
```

`--predicate` syntax is [NSPredicate](https://developer.apple.com/documentation/foundation/nspredicate):
operators include `==`, `!=`, `CONTAINS`, `BEGINSWITH`, `ENDSWITH`,
`MATCHES` (regex), combined with `AND` / `OR` / `NOT`.

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

## Recommended aliases

Drop these in `~/.zshrc` for the iOS equivalent of `adb logcat | grep jrr:`:

```bash
# All app logs, live (iOS simulator)
alias jrrlog='xcrun simctl spawn booted log stream --process JRRKmpnativeui | grep "jrr:"'

# Just network
alias jrrnet='xcrun simctl spawn booted log stream --predicate "category BEGINSWITH \"jrr:net:\""'

# Just errors
alias jrrerr='xcrun simctl spawn booted log stream --process JRRKmpnativeui --level error | grep "jrr:"'

# Last 5 minutes from history (great after reproducing a bug)
alias jrrtail='xcrun simctl spawn booted log show --last 5m --predicate "subsystem == \"com.jrr.jrrkmp-native-ui.JRRKmpnativeui\""'

# Android side, for symmetry
alias jrrlog-android='adb logcat | grep "jrr:"'
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
| All app logs | `adb logcat \| grep 'jrr:'` | `xcrun simctl spawn booted log stream --process JRRKmpnativeui \| grep 'jrr:'` |
| Network only | `adb logcat \| grep 'jrr:net:'` | `xcrun simctl spawn booted log stream --predicate 'category BEGINSWITH "jrr:net:"'` |
| Errors only | `adb logcat *:E \| grep 'jrr:'` | `xcrun simctl spawn booted log stream --process JRRKmpnativeui --level error` |
| Tail last N min | `adb logcat -t '5 minutes ago' \| grep 'jrr:'` | `xcrun simctl spawn booted log show --last 5m --predicate '…'` |
