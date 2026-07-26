package online.slavok.whitelist

import online.slavok.whitelist.config.ConfigManager
import online.slavok.whitelist.database.DatabaseManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

// Same contract as the mod's Brigadier command: add/remove/list/on/off, aliases and
// the permission gate live in plugin.yml (default op). Feedback text/colors match
// the mod exactly.
class WhitelistCommand(
    private val configManager: ConfigManager,
    private val databaseManager: DatabaseManager,
) : CommandExecutor, TabCompleter {

    private val subcommands = listOf("add", "remove", "list", "on", "off")

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.isEmpty()) return usage(sender)
        when (args[0].lowercase()) {
            "add" -> {
                val name = args.getOrNull(1) ?: return usage(sender)
                if (databaseManager.addPlayer(name)) {
                    sender.sendMessage("${ChatColor.GREEN}$name added to whitelist")
                } else {
                    sender.sendMessage("${ChatColor.DARK_RED}$name is already on the whitelist")
                }
            }
            "remove" -> {
                val name = args.getOrNull(1) ?: return usage(sender)
                if (databaseManager.removePlayer(name)) {
                    sender.sendMessage("${ChatColor.RED}$name removed from the whitelist")
                } else {
                    sender.sendMessage("${ChatColor.DARK_RED}$name not found on the whitelist")
                }
            }
            "list" -> {
                val players = databaseManager.getAll()
                sender.sendMessage("${ChatColor.GOLD}Players on the whitelist: ${players.joinToString(", ")}")
            }
            "on" -> {
                configManager.setWhitelist(true)
                sender.sendMessage("${ChatColor.GREEN}Whitelist is enabled")
            }
            "off" -> {
                configManager.setWhitelist(false)
                sender.sendMessage("${ChatColor.RED}Whitelist is disabled")
            }
            else -> return usage(sender)
        }
        return true
    }

    private fun usage(sender: CommandSender): Boolean {
        sender.sendMessage("${ChatColor.YELLOW}Usage: /simplewhitelist <add|remove|list|on|off>")
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        return when (args.size) {
            1 -> subcommands.filter { it.startsWith(args[0].lowercase()) }
            2 -> when (args[0].lowercase()) {
                "add" -> Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1]) }
                "remove" -> databaseManager.getAll().filter { it.startsWith(args[1]) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
