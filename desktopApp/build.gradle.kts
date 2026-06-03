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
            // Decompose root + Essenty lifecycle to construct the component tree.
            implementation(libs.decompose)
            implementation(libs.essenty.lifecycle)
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
