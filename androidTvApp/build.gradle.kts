import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Android TV app — a dedicated 10-foot UI built with Jetpack Compose for TV
 * (`androidx.tv`). Online-only: it reuses the shared ViewModels/repositories
 * (`:sharedLogic`) and the shared Android runtime (`:androidCore`: media3 local
 * player engine + trust-all TLS) but NOT the touch-oriented `:composeUi`.
 *
 * Uses Compose Multiplatform's `compose.*` artifacts (which on Android resolve
 * to androidx Compose) plus `androidx.tv:tv-material` on top.
 */
plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(projects.sharedLogic)
    implementation(projects.androidCore)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // Compose (CMP artifacts → androidx Compose on Android)
    implementation(libs.compose.runtime)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)

    // Jetpack Compose for TV — focus-aware components (TabRow, Card, ListItem, …)
    implementation(libs.androidx.tv.material)

    // Media3 (local playback engine lives in :androidCore; the app starts the
    // ExoPlayer-backed engine through the facade).
    implementation(libs.media3.exoplayer)

    // Coil 3 image loading (artwork) + OkHttp network fetcher for self-signed TLS.
    implementation(libs.coil3.compose)
    implementation(libs.coil3.network.okhttp)

    // Room runtime — a couple of repository paths touch the DB type directly.
    implementation(libs.androidx.room.runtime)

    implementation(libs.kotlinx.serialization.json)
}

android {
    namespace = "com.jrr.jrrkmp_native_ui.tv"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.jrr.jrrkmp_native_ui.tv"
        // Older Android TV boxes (e.g. Mi Box 3 = API 28) run below the phone
        // app's floor; the TV stack needs nothing above API 23.
        minSdk = libs.versions.android.minSdkTv.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
