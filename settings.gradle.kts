rootProject.name = "JRRKmpnativeui"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// Lets Gradle Java toolchains be auto-provisioned by download when no matching
// JDK is installed locally — in particular the JetBrains Runtime (JBR) that
// Compose Hot Reload (:desktopApp `hotRunJvm`/`reload`) requires. On machines
// that already have a JBR (e.g. via Android Studio / IntelliJ), the local one
// is used and nothing is downloaded.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":androidApp")
include(":androidTvApp")
include(":androidCore")
include(":sharedLogic")
include(":composeUi")
include(":desktopApp")