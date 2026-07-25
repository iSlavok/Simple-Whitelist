import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.13-SNAPSHOT"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

// Per-version matrix. Everything that differs between Minecraft versions lives here;
// the shared source in src/ only branches where the touched API actually changes.
data class Mc(
    val yarn: String,
    val flk: String,          // fabric-language-kotlin
    val fapi: String,         // fabric-api
    val permissions: String,  // me.lucko:fabric-permissions-api
    val java: Int,
    val depends: String,      // "minecraft" range for fabric.mod.json
    val gameVersions: List<String>, // published game versions (used by the release pipeline)
)

val mcVersion = stonecutter.current.version
val mc = when (mcVersion) {
    "1.19.4" -> Mc(
        yarn = "1.19.4+build.2",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.87.2+1.19.4",
        permissions = "0.3.1",
        java = 17,
        depends = ">=1.19 <1.20",
        gameVersions = listOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4"),
    )
    "1.20.1" -> Mc(
        yarn = "1.20.1+build.10",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.92.11+1.20.1",
        permissions = "0.3.1",
        java = 17,
        depends = ">=1.20 <1.20.5",
        gameVersions = listOf("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4"),
    )
    "1.20.6" -> Mc(
        yarn = "1.20.6+build.3",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.100.8+1.20.6",
        permissions = "0.3.1",
        java = 21,
        depends = ">=1.20.5 <1.21",
        gameVersions = listOf("1.20.5", "1.20.6"),
    )
    "1.21.10" -> Mc(
        yarn = "1.21.10+build.2",
        flk = "1.11.0+kotlin.2.0.0",
        fapi = "0.138.3+1.21.10",
        permissions = "0.3.1",
        java = 21,
        depends = ">=1.21 <1.22",
        gameVersions = listOf(
            "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4",
            "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10",
        ),
    )
    else -> error("Unconfigured Minecraft version: $mcVersion")
}

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String
base { archivesName.set(property("archives_base_name") as String) }

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${mc.yarn}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${mc.fapi}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${mc.flk}")
    modImplementation("me.lucko:fabric-permissions-api:${mc.permissions}")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("kotlinx_serialization_version")}")
    include("com.mysql:mysql-connector-j:${property("mysql_connector_version")}")
    implementation("com.zaxxer:HikariCP:5.1.0")
    include("com.zaxxer:HikariCP:5.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "java_level" to mc.java,
        "minecraft_dep" to mc.depends,
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(mc.java) }

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(mc.java.toString()))
}

java {
    withSourcesJar()
    val jv = JavaVersion.toVersion(mc.java)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.jar {
    from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } }
}
