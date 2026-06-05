import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Shared Android runtime pieces used by both `:androidApp` (phone/tablet) and
 * `:androidTvApp` (Android TV): the media3 local player engine and the
 * trust-all TLS helper for JRiver's self-signed certificate. Kept Android-only
 * (single `androidLibrary` target) — no JVM/desktop consumer needs media3.
 */
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    androidLibrary {
        namespace = "com.jrr.jrrkmp_native_ui.androidcore"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        // Lower floor so :androidTvApp can target older TV boxes.
        minSdk = libs.versions.android.minSdkTv.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.sharedLogic)
            implementation(libs.media3.exoplayer)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
