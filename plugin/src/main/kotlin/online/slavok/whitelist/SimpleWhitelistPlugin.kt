package online.slavok.whitelist

import online.slavok.whitelist.config.ConfigManager
import online.slavok.whitelist.database.DatabaseManager
import online.slavok.whitelist.database.JsonManager
import online.slavok.whitelist.database.MySqlManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

// open: MockBukkit (ByteBuddy) subclasses this in tests; Kotlin classes are final
// by default, which would throw "Cannot subclass final".
open class SimpleWhitelistPlugin : JavaPlugin() {

    lateinit var configManager: ConfigManager
        private set
    lateinit var databaseManager: DatabaseManager
        private set

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        configManager = ConfigManager(File(dataFolder, "config.json"))
        databaseManager = if (configManager.config.databaseType == "mysql") {
            logger.info("Using MySQL database")
            MySqlManager(configManager.config.mysqlUrl)
        } else {
            logger.info("Using JSON database")
            JsonManager(File(dataFolder, configManager.config.jsonFileName))
        }

        server.pluginManager.registerEvents(
            WhitelistListener(configManager, databaseManager), this,
        )
        val handler = WhitelistCommand(configManager, databaseManager)
        getCommand("simplewhitelist")?.apply {
            setExecutor(handler)
            tabCompleter = handler
        }
    }
}
