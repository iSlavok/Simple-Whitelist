plugins {
    id("dev.kikugie.stonecutter")
}

// Active version for the IDE and single-version tasks. A plain `./gradlew build`
// builds every declared version (chiseled tasks were removed in Stonecutter 0.7).
stonecutter active "1.21.10"
