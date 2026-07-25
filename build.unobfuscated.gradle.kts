import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Build script for the unobfuscated era (Minecraft 26+). No Yarn mappings exist;
// the game ships with Mojang names, so this uses the non-remapping Loom variant.
plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.17.17" // non-remapping variant
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}

data class Unobf(
    val fapi: String,
    val flk: String,
    val runtimeJava: Int,       // Java the game requires at runtime (fabric.mod.json + mixin level)
    val depends: String,
    val gameVersions: List<String>,
)

// Our own bytecode targets 21 (Kotlin 2.0 can't emit 25 yet); Java 21 classes run
// fine on the game's Java 25 runtime. The mixin compatibility level must match the
// game's class version, so it uses runtimeJava.
val compileJava = 21

val mcVersion = stonecutter.current.version
val u = when (mcVersion) {
    "26.2" -> Unobf(
        fapi = "0.155.2+26.2",
        flk = "1.13.13+kotlin.2.4.10",
        runtimeJava = 25,
        depends = ">=26.2 <27",
        gameVersions = listOf("26.2"),
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
    // No mappings() — unobfuscated.
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${u.fapi}")
    // fabric-language-kotlin is a runtime-only language adapter (declared in
    // fabric.mod.json depends and provided by the user). It is deliberately NOT on
    // the compile classpath: flk ${u.flk} bundles kotlin-stdlib 2.4.10, whose
    // metadata our Kotlin 2.0 compiler can't read. Our code never references flk.
    // fabric-permissions-api is not published for 26+; commands fall back to the
    // vanilla operator level on this version (see the >=1.22 branch in Permission).

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
        "java_level" to u.runtimeJava,
        "minecraft_dep" to u.depends,
        "modmenu_entrypoint" to "", // no ModMenu integration on 26+ yet
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }
}

tasks.withType<JavaCompile>().configureEach { options.release.set(compileJava) }

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(compileJava.toString()))
}

java {
    withSourcesJar()
    val jv = JavaVersion.toVersion(compileJava)
    sourceCompatibility = jv
    targetCompatibility = jv
}

tasks.jar {
    from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } }
}

// Non-remapping build: publish the plain `jar` (there is no remapJar here).
publishMods {
    file.set(tasks.named<AbstractArchiveTask>("jar").flatMap { it.archiveFile })
    version.set(project.version.toString())
    type.set(me.modmuss50.mpp.ReleaseType.STABLE)
    modLoaders.add("fabric")
    changelog.set("See https://github.com/iSlavok/Simple-Whitelist/releases")
    modrinth {
        projectId.set(providers.gradleProperty("modrinth_id"))
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.addAll(u.gameVersions)
        requires("fabric-api")
        requires("fabric-language-kotlin")
    }
}
