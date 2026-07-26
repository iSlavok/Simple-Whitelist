plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "3.0.2"
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

    // Shaded runtime libs (relocated in the shadowJar config below).
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("com.mysql:mysql-connector-j:9.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    // MockBukkit + paper-api (versions aligned in Task 3).
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
