# Desktop on Linux — build, run, package

The desktop app is the **Compose Multiplatform `jvm()`** target (the same module
that ships the Windows build — see [desktop-compose-plan.md](desktop-compose-plan.md)).
Because it's a JVM app, it runs on Linux x64/arm64 with a JDK; there is no
Kotlin/Native Linux target (same rationale as Windows — Room/Decompose/Essenty
publish no Kotlin/Native desktop klibs; see Appendix A of the desktop plan).

This doc covers building, running, and packaging on Linux, including under
**WSL (WSLg)** on Windows.

## Prerequisites

- **JDK 17+** to launch Gradle (the build's Gradle toolchain auto-provisions the
  JBR that Compose Hot Reload needs; a base JDK is still required to start Gradle).
- **Local audio playback** needs system **libvlc** (VLCJ discovers it at runtime):
  - Debian/Ubuntu: `sudo apt install -y vlc`
  - Fedora: `sudo dnf install vlc`
  - …or bundle it into the build instead (see [Bundling libvlc](#bundling-libvlc)).
  The app launches fine without it — local playback just logs *"libvlc not
  found"*; remote MCWS zone control is unaffected.
- **Building installers** needs the jpackage backends on `PATH`:
  - `.deb` → `fakeroot` (`sudo apt install -y fakeroot`)
  - `.rpm` → `rpmbuild` (`sudo apt install -y rpm`)

## Build & run

```bash
# Run from source (dev loop — recompiles, then launches the window)
./gradlew :desktopApp:run

# Assemble a self-contained app image (native launcher + bundled JRE)
./gradlew :desktopApp:createDistributable
# -> desktopApp/build/compose/binaries/main/app/JRRDesktop/bin/JRRDesktop

# Run that assembled image via Gradle
./gradlew :desktopApp:runDistributable
```

## Packaging installers

```bash
./gradlew :desktopApp:packageDeb   # -> desktopApp/build/compose/binaries/main/deb/
./gradlew :desktopApp:packageRpm   # -> desktopApp/build/compose/binaries/main/rpm/
```

The image bundles a JRE (`includeAllModules = true`), so installs need no system
Java. If `fakeroot`/`rpmbuild` is missing, jpackage skips the bundler with
*"Can not find fakeroot"* (or the rpm equivalent) and the task fails — install the
tool above and re-run.

## Bundling libvlc

By default the packaged app discovers a **system** libvlc at runtime. To ship a
**self-contained** app that needs no system VLC, stage libvlc into the image with
`-PvlcHome` (or the `VLC_HOME` env var):

```bash
./gradlew :desktopApp:packageDeb -PvlcHome=/usr/lib/x86_64-linux-gnu
```

- `syncVlcNatives` stages `libvlc.so*` + `libvlccore.so*` and the `plugins/` tree
  into `linux-x64/vlc/`, and creates the unversioned `libvlc.so` / `libvlccore.so`
  symlinks JNA loads by (runtime VLC ships only `libvlc.so.5`).
- Point `-PvlcHome` at a dir containing `libvlc.so*`. The plugins are picked up
  from either `<vlcHome>/plugins` or `<vlcHome>/vlc/plugins` (Debian/Ubuntu use
  the latter), so `/usr/lib/x86_64-linux-gnu` works out of the box. Default if
  unset: `/usr/lib/vlc`.
- At runtime `DesktopPlayerEngine.configureBundledNatives` points JNA at
  `<resources>/vlc`; vlcj's `LinuxNativeDiscoveryStrategy` then sets
  `VLC_PLUGIN_PATH` to `<libdir>/plugins`.
- If no VLC is found at build time the staging is skipped (with a warning) and the
  packaged app falls back to a system libvlc at runtime.

## Running under WSL (WSLg)

On **Windows 11**, WSL2 + WSLg can build and run the Linux app, displaying the
window on the Windows desktop and routing audio back to Windows. Verified on
Ubuntu under WSL2.

- **Build in the WSL-native filesystem** (e.g. `~/…`), not `/mnt/c/…` — the 9p
  mount is slow and trips Gradle's file watching/locking.
- **Force software rendering** — WSLg can't give Skiko an OpenGL context
  (`org.jetbrains.skiko.RenderException: Cannot create Linux GL context`):

  ```bash
  # from source
  JAVA_TOOL_OPTIONS=-Dskiko.renderApi=SOFTWARE ./gradlew :desktopApp:run

  # or the assembled image
  cd desktopApp/build/compose/binaries/main/app/JRRDesktop
  JAVA_TOOL_OPTIONS=-Dskiko.renderApi=SOFTWARE ./bin/JRRDesktop
  ```

- **Audio**: `sudo apt install -y vlc`; output routes through WSLg's PulseAudio
  bridge to Windows.
- **Networking**: WSL2 uses NAT — a JRiver server elsewhere on the LAN is
  reachable, but a server on the **Windows host** is not at `localhost` (use the
  host IP, or enable mirrored networking in `.wslconfig`).
- **Media keys**: the global Play/Pause/Next/Prev hook is Windows-only and
  no-ops on Linux (logged as *"media-key hook skipped (non-Windows host)"*).

## See also

- [desktop-compose-plan.md](desktop-compose-plan.md) — desktop architecture and
  the phased Windows implementation (packaging lives in Phase 5).
- [macos-target-plan.md](macos-target-plan.md) — the native macOS path.
