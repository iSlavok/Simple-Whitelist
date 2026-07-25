package online.slavok.whitelist.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import online.slavok.whitelist.SimpleWhitelist
import online.slavok.whitelist.literalText
import online.slavok.whitelist.commands.api.Permission
import online.slavok.whitelist.commands.suggestionProviders.PlayerSuggestionProvider
import online.slavok.whitelist.commands.suggestionProviders.WhitelistPlayerSuggestionProvider

// ServerCommandSource.sendFeedback changed signature in 1.20: pre-1.20 takes a
// Text directly, 1.20+ takes a Supplier<Text>. Branch once here instead of at
// every call site.
private fun ServerCommandSource.feedback(text: Text, broadcastToOps: Boolean) {
    //? if >=1.20 {
    sendFeedback({ text }, broadcastToOps)
    //?} else {
    /*sendFeedback(text, broadcastToOps)*/
    //?}
}

class SimpleWhitelistCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
        val command = dispatcher.register(
            literal("simplewhitelist")
                .requires(Permission.require("simplewhitelist.command", 4))
                .then(
                    literal("add")
                    .then(CommandManager.argument("nickname", StringArgumentType.string())
                        .suggests(PlayerSuggestionProvider())
                        .executes { context ->
                            val playerName = context.getArgument("nickname", String::class.java)
                            if (SimpleWhitelist.databaseManager.addPlayer(playerName)) {
                                context.source.feedback(
                                    literalText("$playerName added to whitelist").formatted(Formatting.GREEN),
                                    true
                                )
                                1
                            } else {
                                context.source.feedback(
                                    literalText("$playerName is already on the whitelist").formatted(Formatting.DARK_RED),
                                    true
                                )
                                0
                            }
                        }
                    )
                ).then(
                    literal("remove")
                    .then(CommandManager.argument("nickname", StringArgumentType.string())
                        .suggests(WhitelistPlayerSuggestionProvider())
                        .executes { context ->
                            val playerName = context.getArgument("nickname", String::class.java)
                            if (SimpleWhitelist.databaseManager.removePlayer(playerName)) {
                                context.source.feedback(
                                    literalText("$playerName removed from the whitelist").formatted(Formatting.RED),
                                    true
                                )
                                1
                            } else {
                                context.source.feedback(
                                    literalText("$playerName not found on the whitelist").formatted(Formatting.DARK_RED),
                                    true
                                )
                                0
                            }
                        }
                    )
                ).then(
                    literal("list")
                        .executes { context ->
                            val players = SimpleWhitelist.databaseManager.getAll()
                            context.source.feedback(
                                literalText("Players on the whitelist: ${players.joinToString(", ")}").formatted(Formatting.GOLD),
                                false
                            )
                            1
                        }
                ).then(
                    literal("on")
                        .executes { context ->
                            SimpleWhitelist.configManager.setWhitelist(true)
                            context.source.feedback(
                                literalText("Whitelist is enabled").formatted(Formatting.GREEN),
                                true
                            )
                            1
                        }
                ).then(
                    literal("off")
                        .executes { context ->
                            SimpleWhitelist.configManager.setWhitelist(false)
                            context.source.feedback(
                                literalText("Whitelist is disabled").formatted(Formatting.RED),
                                true
                            )
                            1
                        }
                )
        )
        dispatcher.register(literal("swh").redirect(command))
        dispatcher.register(literal("swl").redirect(command))
        dispatcher.register(literal("swhitelist").redirect(command))
    }
}
