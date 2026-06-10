plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
}

// Warning gate for the pre-push hook: `./gradlew check -PstrictWarnings`
// fails any Kotlin compilation that emits a warning. Gated behind a property
// so day-to-day IDE/dev builds stay relaxed while work is in progress.
// Note: toggling the property changes compiler args, so the first strict run
// recompiles everything — that full pass is what makes the check complete.
val strictWarnings = providers.gradleProperty("strictWarnings").isPresent
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
        compilerOptions.allWarningsAsErrors.set(strictWarnings)
    }
}