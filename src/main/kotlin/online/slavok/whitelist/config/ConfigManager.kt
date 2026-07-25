package online.slavok.whitelist.config

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.slavok.whitelist.SimpleWhitelist
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class ConfigManager(
    private val configFile: File,
) {
    private val lock = Any()
    private val json = Json { prettyPrint = true }

    var config = Config(
        true,
        "json",
        "jdbc:mysql://<username>:<password>@<host>:<port>/<database>",
        "whitelist.json"
    )
        private set

    init {
        if (configFile.exists()) {
            try {
                config = json.decodeFromString(configFile.readText())
            } catch (e: Exception) {
                SimpleWhitelist.logger.warn("Error loading config: ${e.message}")
            }
        }
        saveConfig()
    }

    fun setWhitelist(enabled: Boolean) = synchronized(lock) {
        config.whitelist = enabled
        saveConfig()
    }

    private fun saveConfig() = synchronized(lock) {
        val tmp = File(configFile.parentFile, "${configFile.name}.tmp")
        tmp.writeText(json.encodeToString(config))
        Files.move(
            tmp.toPath(), configFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE,
        )
    }
}
