pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        // Each anchor's jar covers a whole patch band via the version range in
        // fabric.mod.json. More anchors are added in later PRs.
        versions("1.20.1", "1.20.6", "1.21.10")
        vcsVersion = "1.21.10"
    }
}

rootProject.name = "simple-whitelist"
