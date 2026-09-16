rootProject.name = "FKCAC"

pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            mavenContent {
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
                includeGroup("com.gtnewhorizons")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    // Automatic toolchain provisioning (Java 8 is not bundled on modern runners)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}