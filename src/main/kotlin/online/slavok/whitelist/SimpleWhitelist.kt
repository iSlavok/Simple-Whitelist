package online.slavok.whitelist

import net.fabricmc.api.ModInitializer
//? if >=1.19 {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
//?} else {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback*/
//?}
import net.fabricmc.loader.api.FabricLoader
import online.slavok.whitelist.commands.SimpleWhitelistCommand
import online.slavok.whitelist.config.ConfigManager
import online.slavok.whitelist.database.DatabaseManager
import online.slavok.whitelist.database.JsonManager
import online.slavok.whitelist.database.MySqlManager
import online.slavok.whitelist.events.LoginEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files


object SimpleWhitelist : ModInitializer {
	val logger: Logger = LoggerFactory.getLogger("simple-whitelist")
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

		// Command API v2 (1.19+) passes (dispatcher, registryAccess, environment);
		// v1 (1.18 and earlier) passes (dispatcher, dedicated).
		//? if >=1.19 {
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			SimpleWhitelistCommand().register(dispatcher)
		}
		//?} else {
		/*CommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			SimpleWhitelistCommand().register(dispatcher)
		}*/
		//?}
	}
}
