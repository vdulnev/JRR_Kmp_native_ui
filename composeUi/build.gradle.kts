import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // Quiet the Beta warning for expect/actual classes & objects (used by the
    // platform font families in core/theme/AppFonts).
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    // Desktop (JVM) — consumed by :desktopApp.
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    // Android — consumed by :androidApp. Holds the shared Compose UI so both
    // hosts render the same screens/components/theme.
    androidLibrary {
        namespace = "com.jrr.jrrkmp_native_ui.composeui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        // Font files in androidMain/res/font are loaded via R.font in the
        // Android `actual` of AppFonts.
        androidResources {
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.sharedLogic)
            // Lifecycle: ViewModel supertype of the feature VMs, plus
            // collectAsStateWithLifecycle used by the screens. (sharedLogic keeps
            // these as `implementation`, so they aren't transitive here.)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Room runtime: a couple of screens read the DB directly (LibraryScreen
            // favorites), so JrrDatabase's RoomDatabase supertype must be visible.
            implementation(libs.androidx.room.runtime)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            // Extended Material icons used across the shared screens (Headphones,
            // Shuffle, Repeat, Volume*, MoreHoriz, …). Same `androidx.compose.
            // material.icons` package as the androidx artifact, so screen imports
            // resolve unchanged once moved to commonMain.
            implementation(compose.materialIconsExtended)
            // Coil 3 (multiplatform) — AsyncImage for shared components/screens.
            // The network fetcher + singleton ImageLoader are configured per host.
            implementation(libs.coil3.compose)
            // Decompose Compose bindings — subscribeAsState/Children over the
            // shared component tree (same integration the Android host uses).
            implementation(libs.decompose)
            implementation(libs.decompose.extensions.compose)
        }
        androidMain.dependencies {
            // For the Android `actual` of BackHandler (system back dispatcher).
            implementation(libs.androidx.activity.compose)
        }
    }
}
