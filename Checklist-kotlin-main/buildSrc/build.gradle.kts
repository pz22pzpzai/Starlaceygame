plugins {
    `kotlin-dsl`
}
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.agp)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

kotlin {
    // Use the host JDK (21 in CI) to avoid requiring a separate JDK 17
    // installation. The compiler still targets Java 17 via jvmTarget above.
    // Use JDK 17 to align with the project toolchain and avoid requiring Java 21.
    jvmToolchain(17)
}
