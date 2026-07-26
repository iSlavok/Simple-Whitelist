plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

group = "online.slavok"
version = providers.gradleProperty("plugin_version").getOrElse("1.0.4")

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") { name = "spigot-snapshots" }
    maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
    maven("https://oss.sonatype.org/content/repositories/snapshots/") { name = "sonatype" }
}

dependencies {
    // Provided by the server — never bundled. One jar built against the Bukkit API
    // runs on Spigot/Paper/Purpur.
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")

    // SLF4J is provided by the server at runtime (Paper/Spigot ship slf4j-api;
    // JavaPlugin.getSLF4JLogger exists on api-version 1.20+), so never bundle it.
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    // Shaded runtime libs (relocated in the shadowJar config below).
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.mysql:mysql-connector-j:9.0.0")

    // JUnit 6 BOM — MockBukkit-v1.21 is built against JUnit 6, so the whole suite
    // rides on it (the BOM keeps jupiter + platform-launcher versions aligned).
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.slf4j:slf4j-api:2.0.9")
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    // MockBukkit target patch and paper-api must match (else IncompatiblePaperVersionException).
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    filesMatching("plugin.yml") { expand(props) }
}

tasks.shadowJar {
    archiveClassifier.set("")
    // Relocate HikariCP — the library most likely to clash with other DB plugins.
    // mysql-connector is left un-relocated (its META-INF/services JDBC driver entry
    // is preserved by mergeServiceFiles and Hikari resolves it via the jdbcUrl).
    // kotlin-stdlib is bundled but not relocated — Paper isolates each plugin in its
    // own classloader.
    relocate("com.zaxxer.hikari", "online.slavok.whitelist.libs.hikari")
    mergeServiceFiles() // keep META-INF/services/java.sql.Driver so MySQL loads
}

// shadowJar is the published artifact; disable the thin jar to avoid a name clash
// (both would be simple-whitelist-plugin-<version>.jar).
tasks.jar { enabled = false }
tasks.assemble { dependsOn(tasks.shadowJar) }

// Publish the plugin jar to the SAME Modrinth project as the mod, under plugin loaders.
// The version string carries a `+plugin` suffix so it never collides with the mod nodes'
// `<version>+mcX.Y.Z` (Modrinth version numbers must be unique per project).
publishMods {
    file.set(tasks.shadowJar.flatMap { it.archiveFile })
    version.set("${project.version}+plugin")
    displayName.set("Simple Whitelist (plugin) ${project.version}")
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.addAll("bukkit", "spigot", "paper", "purpur", "folia")
    changelog.set("See https://github.com/iSlavok/Simple-Whitelist/releases")
    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        // Stable Bukkit API — one jar spans the range; mod-publish resolves the concrete
        // version list from Modrinth at publish time. Start at 1.20 (our plugin.yml
        // api-version); widen `end` once the plugin is verified on newer servers.
        minecraftVersionRange {
            start = "1.20"
            end = "1.21.11"
        }
    }
}
