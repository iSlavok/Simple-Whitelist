import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.17.17"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
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
    // In-game gametests need fabric-gametest-api-v1; loom pulls a fixed gametest
    // fabric-api (0.97.0+1.20.4) that only wins the version conflict when this
    // anchor's own fabric-api is at least that new. Older anchors rely on the unit
    // tests (which run on every version) instead.
    val gametest: Boolean = false,
    // Optional ModMenu integration (client config screen). Set to the ModMenu
    // version on anchors where the screen source compiles; null elsewhere.
    val modmenu: String? = null,
)

val mcVersion = stonecutter.current.version
val mc = when (mcVersion) {
    "1.18.2" -> Mc(
        yarn = "1.18.2+build.4",
        flk = "1.12.3+kotlin.2.0.21",
        fapi = "0.77.0+1.18.2",
        permissions = "0.3.1",
        java = 17,
        depends = ">=1.18.2 <1.19",
        gameVersions = listOf("1.18.2"),
    )
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
        gametest = true,
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
        gametest = true,
        modmenu = "16.0.1",
    )
    else -> error("Unconfigured Minecraft version: $mcVersion")
}

version = "${property("mod_version")}+mc$mcVersion"
group = property("maven_group") as String
base { archivesName.set(property("archives_base_name") as String) }

repositories {
    mavenCentral()
    maven("https://maven.terraformersmc.com/releases/") // ModMenu
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:${mc.yarn}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${mc.fapi}")
    mc.modmenu?.let { modImplementation("com.terraformersmc:modmenu:$it") }
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

// Headless in-game gametests (booted server) — see src/gametest. Enabled only on
// anchors whose fabric-api ships a compatible fabric-gametest-api-v1 (see the
// gametest flag in the matrix); other versions are covered by the unit tests.
if (mc.gametest) {
    fabricApi {
        configureTests {
            createSourceSet = true
            modId = "${property("archives_base_name")}-test"
            eula = true
        }
    }
    dependencies {
        "modGametestImplementation"("net.fabricmc.fabric-api:fabric-api:${mc.fapi}")
    }
}

tasks.processResources {
    // Inject the ModMenu entrypoint only where the integration is compiled in.
    val modmenuEntrypoint = if (mc.modmenu != null) {
        ",\n\t\t\"modmenu\": [\"online.slavok.whitelist.modmenu.WhitelistModMenu\"]"
    } else {
        ""
    }
    val props = mapOf(
        "version" to project.version,
        "java_level" to mc.java,
        "minecraft_dep" to mc.depends,
        "modmenu_entrypoint" to modmenuEntrypoint,
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

// Publish this version node to Modrinth (run for all nodes via `./gradlew publishMods`).
publishMods {
    file.set(tasks.named<AbstractArchiveTask>("remapJar").flatMap { it.archiveFile })
    version.set(project.version.toString())
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.add("fabric")
    changelog.set("See https://github.com/iSlavok/Simple-Whitelist/releases")
    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        // Must be the exact, complete patch list — Modrinth shows only what is listed.
        minecraftVersions.addAll(mc.gameVersions)
        requires("fabric-api")
        requires("fabric-language-kotlin")
        if (mc.modmenu != null) optional("modmenu")
    }
}
