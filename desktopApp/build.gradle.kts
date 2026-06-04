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
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.jrr.jrrkmp_native_ui.desktop.MainKt"
        nativeDistributions {
            // Packaging is fleshed out in Phase 5; declared here so `:desktopApp`
            // is a complete desktop application module from the start.
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "JRRDesktop"
            packageVersion = "1.0.0"
        }
    }
}
