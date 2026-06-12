import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.composeUi)
            implementation(projects.sharedLogic)
            // Skiko-backed Compose runtime for the current host OS.
            implementation(compose.desktop.currentOs)
            // Provides Dispatchers.Main on the JVM, backed by the AWT event
            // dispatch thread (where Compose Desktop runs composition). Without
            // it AudioPlayerFacade's `Dispatchers.Main` scope throws at startup.
            implementation(libs.kotlinx.coroutines.swing)
            // Decompose root + Essenty lifecycle to construct the component tree.
            implementation(libs.decompose)
            implementation(libs.essenty.lifecycle)
            // Room types leak through DesktopAppContainer's API (DatabaseBuilder
            // returns RoomDatabase.Builder); the bundled SQLite driver backs the
            // on-disk DB at runtime. Both are `implementation` in sharedLogic, so
            // not transitive — declare them here for the desktop host too.
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            // The feature ViewModels extend androidx.lifecycle.ViewModel; the dep
            // is `implementation` (non-transitive) in sharedLogic, so the desktop
            // host needs it on its compile classpath to construct them.
            implementation(libs.androidx.lifecycle.viewmodel)
            // VLCJ (libvlc bindings) backs on-device audio playback. The libvlc
            // natives themselves are bundled into the app image — see
            // `syncVlcNatives` below.
            implementation(libs.vlcj)
        }
    }
}

// --- libvlc native bundling -------------------------------------------------
//
// The libvlc binaries (core libs and the ~100MB `plugins/` tree) are NOT on
// Maven and are too large to vendor in git, so they are staged at build time
// from a local VLC install into the Compose app-resources dir. They are then
// discovered at runtime via `compose.application.resources.dir` (see
// DesktopPlayerEngine.configureBundledNatives) — no system VLC required by the
// end user.
//
// Source VLC install resolution order: -PvlcHome=… > VLC_HOME env > the
// platform default install path. If none is found the task is skipped and the
// app falls back to discovering a system VLC at runtime.
//
// Per-OS layout inside the staged `vlc/` folder:
//  - Windows: libvlc.dll + libvlccore.dll at the root, plugins/ beside them
//    (libvlc finds its plugins relative to the DLL).
//  - macOS: VLC.app's own layout — lib/*.dylib with plugins/ as a sibling.
//    vlcj's OsxNativeDiscoveryStrategy derives VLC_PLUGIN_PATH as
//    `<libdir>/../plugins`, and the dylibs resolve each other via @rpath in
//    the same dir, so the layout must be preserved verbatim.
val hostOsName: String = System.getProperty("os.name").lowercase()
val isMacHost: Boolean = hostOsName.contains("mac")
val isWindowsHost: Boolean = hostOsName.contains("windows")
val hostArch: String =
    if (System.getProperty("os.arch") in setOf("aarch64", "arm64")) "arm64" else "x64"

// Compose copies appResourcesRootDir/<os>-<arch>/** into the runtime resources
// dir for that target.
val composeResourcesOsDir: String = when {
    isMacHost -> "macos-$hostArch"
    isWindowsHost -> "windows-x64"
    else -> "linux-$hostArch"
}

val vlcHome: String = (project.findProperty("vlcHome") as String?)
    ?: System.getenv("VLC_HOME")
    ?: when {
        isMacHost -> "/Applications/VLC.app/Contents/MacOS"
        isWindowsHost -> "C:/Program Files/VideoLAN/VLC"
        else -> "/usr/lib/vlc"
    }

val vlcResourcesDir = layout.buildDirectory.dir("vlcResources")

val syncVlcNatives by tasks.registering(Copy::class) {
    group = "compose desktop"
    description =
        "Stages libvlc natives (core libs + plugins) from a VLC install into the app resources for bundling."

    // Local copies of the top-level script vals: task closures (onlyIf, the
    // `into` provider) must not capture the script object — the configuration
    // cache cannot serialize it.
    val home = file(vlcHome)
    val macHost = isMacHost
    val osDirName = composeResourcesOsDir
    val libvlc = if (macHost) home.resolve("lib/libvlc.dylib") else home.resolve("libvlc.dll")
    val wantedArch = if (hostArch == "arm64") "arm64" else "x86_64"
    onlyIf {
        // Architectures baked into a Mach-O binary, via `lipo -archs` (e.g.
        // `[x86_64]`, `[arm64, x86_64]` for universal). Empty means "unknown"
        // (lipo unavailable), which is treated as a pass, not a mismatch.
        // Local fun: top-level script helpers can't be captured by task
        // closures under the configuration cache.
        fun machOArchs(binary: File): Set<String> = try {
            val process = ProcessBuilder("lipo", "-archs", binary.absolutePath)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            if (process.waitFor() == 0) output.split(Regex("\\s+")).toSet() else emptySet()
        } catch (_: Exception) {
            emptySet()
        }

        if (!libvlc.exists()) {
            logger.warn(
                "syncVlcNatives: no ${libvlc.name} under '$home'. Set -PvlcHome=<VLC dir> or " +
                    "VLC_HOME. The packaged app will fall back to a system VLC install at runtime."
            )
            return@onlyIf false
        }
        // VLC for macOS ships separate Intel/Apple Silicon builds; bundling the
        // wrong one would fail to load into this host's JVM at runtime.
        if (macHost) {
            val archs = machOArchs(libvlc)
            if (archs.isNotEmpty() && wantedArch !in archs) {
                logger.warn(
                    "syncVlcNatives: VLC at '$home' is built for $archs but this host is " +
                        "$wantedArch. Install the matching VLC build (videolan.org ships " +
                        "separate Intel/Apple Silicon dmgs) or point -PvlcHome/VLC_HOME at one. " +
                        "Skipping — the app will fall back to a system VLC at runtime."
                )
                return@onlyIf false
            }
        }
        true
    }

    if (macHost) {
        from(home.resolve("lib")) {
            include("*.dylib")
            into("lib")
        }
        from(home.resolve("plugins")) { into("plugins") }
    } else {
        from(home) { include("libvlc.dll", "libvlccore.dll") }
        from(home.resolve("plugins")) { into("plugins") }
    }
    into(vlcResourcesDir.map { it.dir("$osDirName/vlc") })
}

// Ensure the natives are staged before Compose collects app resources (for both
// `run` and the packaging tasks).
tasks.matching { it.name == "prepareAppResources" }.configureEach {
    dependsOn(syncVlcNatives)
}

compose.desktop {
    application {
        mainClass = "com.jrr.jrrkmp_native_ui.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Dmg)
            packageName = "JRRDesktop"
            packageVersion = "1.0.0"
            description = "JRiver Media Center remote & local player"
            vendor = "JRR"
            copyright = "© 2026 JRR"

            // Stage the bundled libvlc natives into the installed app image.
            appResourcesRootDir.set(vlcResourcesDir)

            // Ship the full JDK module set so JNA/VLCJ reflection, Room, and
            // Skiko all resolve at runtime without hand-enumerating modules.
            // (Trim to a `modules(...)` allowlist later to shrink the image.)
            includeAllModules = true

            macOS {
                iconFile.set(project.file("packaging/jrr.icns"))
            }

            linux {
                iconFile.set(project.file("packaging/jrr.png"))
            }

            windows {
                menuGroup = "JRR"
                menu = true
                shortcut = true
                perUserInstall = true
                // Stable across releases so installers upgrade in place rather
                // than landing side-by-side. Do not regenerate.
                upgradeUuid = "7b2c1a64-5e8d-4f93-a1c6-0d9e2f4b6a83"
                iconFile.set(project.file("packaging/jrr.ico"))
            }
        }
    }
}
