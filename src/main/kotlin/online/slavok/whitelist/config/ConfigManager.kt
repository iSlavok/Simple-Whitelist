package online.slavok.whitelist.config

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.slavok.whitelist.SimpleWhitelist
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class ConfigManager(
    private val configFile: File,
) {
    var config = Config(
        true,
        "json",
        "jdbc:mysql://<username>:<password>@<host>:<port>/<database>",
        "whitelist.json"
    )
    private val json = Json { prettyPrint = true }

    init {
        if (configFile.exists()) {
            val reader = FileReader(configFile)
            val content = reader.readText()
            reader.close()
            try {
                config = json.decodeFromString(content)
            } catch (e: Exception) {
                SimpleWhitelist.logger.warn("Error loading config: ${e.message}")
            }
        }
        saveConfig()
    }

    fun setWhitelist(enabled: Boolean) {
        config.whitelist = enabled
        saveConfig()
    }


    private fun saveConfig() {
        val writer = FileWriter(configFile)
        val json = json.encodeToString(config)
        writer.write(json)
        writer.flush()
        writer.close()
    }
}