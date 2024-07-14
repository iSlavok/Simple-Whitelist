package online.slavok

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import online.slavok.commands.SimpleWhitelistCommand
import online.slavok.config.ConfigManager
import online.slavok.database.DatabaseManager
import online.slavok.database.JsonManager
import online.slavok.database.MySqlManager
import online.slavok.events.LoginEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path


object SimpleWhitelist : ModInitializer {
	val logger: Logger = LoggerFactory.getLogger("simple-whitelist")
	val advancementsPath: Path = FabricLoader.getInstance().gameDir.resolve("world/advancements")
	val playerDataPath: Path = FabricLoader.getInstance().gameDir.resolve("world/playerdata")
	val statsPath: Path = FabricLoader.getInstance().gameDir.resolve("world/stats")
	lateinit var databaseManager: DatabaseManager
	lateinit var configManager: ConfigManager

	override fun onInitialize() {
		LoginEvent().register()
		val configDir = FabricLoader.getInstance().configDir
		val whitelistDir = configDir.resolve("SimpleWhitelist")
		if (!Files.exists(whitelistDir)) {
			Files.createDirectories(whitelistDir)
		}
		configManager = ConfigManager(whitelistDir.resolve("config.json").toFile())
		if (configManager.config.databaseType == "mysql") {
			databaseManager = MySqlManager(configManager.config.mysqlUrl)
			logger.info("Using MySQL database")
		} else {
			databaseManager = JsonManager(whitelistDir.resolve(configManager.config.jsonFileName).toFile())
			logger.info("Using JSON database")
		}

		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			SimpleWhitelistCommand().register(dispatcher)
		}
	}
}
