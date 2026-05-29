import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.skie)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    // 'expect'/'actual' classes are still flagged as Beta with a noisy
    // compiler warning; the feature is stable in practice. This flag quiets
    // it for every Kotlin compilation in this module.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "com.jrr.jrrkmp_native_ui.sharedLogic"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.lifecycle.viewmodel)
            // `api` so app modules can use Logger.withTag(...) without an
            // explicit Kermit dep of their own — the logging conventions live
            // in commonMain so the deps should travel with them.
            api(libs.kermit)
            implementation(libs.atomicfu)
            // `api` so app modules (and the iOS framework via SKIE) can name
            // Value, ChildStack, ComponentContext without re-declaring the dep.
            api(libs.decompose)
            api(libs.essenty.lifecycle)
            api(libs.essenty.state.keeper)
            api(libs.essenty.back.handler)
            api(libs.essenty.instance.keeper)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}