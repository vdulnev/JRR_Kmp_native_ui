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
// The libvlc binaries (libvlc.dll, libvlccore.dll, and the ~130MB `plugins/`
// tree) are NOT on Maven and are too large to vendor in git, so they are staged
// at build time from a local VLC install into the Compose app-resources dir.
// They are then discovered at runtime via `compose.application.resources.dir`
// (see DesktopPlayerEngine.configureBundledNatives) — no system VLC required by
// the end user.
//
// Source VLC install resolution order: -PvlcHome=… > VLC_HOME env > default
// Windows install path. If none is found the task is skipped and the app falls
// back to discovering a system VLC at runtime.
val vlcHome: String = (project.findProperty("vlcHome") as String?)
    ?: System.getenv("VLC_HOME")
    ?: "C:/Program Files/VideoLAN/VLC"

val vlcResourcesDir = layout.buildDirectory.dir("vlcResources")

val syncVlcNatives by tasks.registering(Copy::class) {
    group = "compose desktop"
    description = "Stages libvlc natives (DLLs + plugins) from a VLC install into the app resources for bundling."

    val home = file(vlcHome)
    onlyIf {
        val present = home.resolve("libvlc.dll").exists()
        if (!present) {
            logger.warn(
                "syncVlcNatives: no libvlc.dll under '$home'. Set -PvlcHome=<VLC dir> or " +
                    "VLC_HOME. The packaged app will fall back to a system VLC install at runtime."
            )
        }
        present
    }

    // libvlc finds its plugins relative to the DLL on Windows, so place the two
    // core DLLs and the plugins/ tree together under a `vlc/` folder.
    from(home) { include("libvlc.dll", "libvlccore.dll") }
    from(home.resolve("plugins")) { into("plugins") }
    // Compose copies appResourcesRootDir/<os>-<arch>/** into the runtime
    // resources dir for that target.
    into(vlcResourcesDir.map { it.dir("windows-x64/vlc") })
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
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
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
