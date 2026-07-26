package online.slavok.whitelist

import online.slavok.whitelist.config.ConfigManager
import online.slavok.whitelist.database.DatabaseManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

// Mirrors the mod's QUERY_START login gate: name-based, case-sensitive. Runs on the
// async pre-login thread — safe because the DB layer is thread-safe (synchronized
// JSON / Hikari pool).
class WhitelistListener(
    private val configManager: ConfigManager,
    private val databaseManager: DatabaseManager,
) : Listener {

    @EventHandler(priority = EventPriority.HIGH)
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (configManager.config.whitelist && !databaseManager.inWhitelist(event.name)) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                "You are not on the whitelist!",
            )
        }
    }
}
