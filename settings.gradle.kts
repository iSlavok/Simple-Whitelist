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
        // One anchor for now. Whole 1.21.x line is covered by this jar via the
        // version range in fabric.mod.json. More anchors are added in later PRs.
        versions("1.21.10")
        vcsVersion = "1.21.10"
    }
}

rootProject.name = "simple-whitelist"
