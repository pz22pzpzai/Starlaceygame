pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.12.2"
        id("com.android.library") version "8.12.2"
        id("org.jetbrains.kotlin.android") version "2.0.10"
        id("org.jetbrains.kotlin.jvm") version "2.0.10"
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = ("kotlin-android-template")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "app",
    "library-android",
    "library-compose",
    "library-kotlin"
)
