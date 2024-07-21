package online.slavok.whitelist.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import online.slavok.whitelist.SimpleWhitelist
import online.slavok.whitelist.commands.api.Permission
import online.slavok.whitelist.commands.suggestionProviders.PlayerSuggestionProvider
import online.slavok.whitelist.commands.suggestionProviders.WhitelistPlayerSuggestionProvider


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
                                context.source.sendFeedback(
                                    { Text.literal("$playerName added to whitelist").formatted(Formatting.GREEN) },
                                    true
                                )
                            } else {
                                context.source.sendFeedback(
                                    { Text.literal("$playerName is already on the whitelist").formatted(Formatting.DARK_RED) },
                                    true
                                )
                            }
                            0
                        }
                    )
                ).then(
                    literal("remove")
                    .then(CommandManager.argument("nickname", StringArgumentType.string())
                        .suggests(WhitelistPlayerSuggestionProvider())
                        .executes { context ->
                            val playerName = context.getArgument("nickname", String::class.java)
                            if (SimpleWhitelist.databaseManager.removePlayer(playerName)) {
                                context.source.sendFeedback(
                                    { Text.literal("$playerName removed from the whitelist").formatted(Formatting.RED) },
                                    true
                                )
                            } else {
                                context.source.sendFeedback(
                                    { Text.literal("$playerName not found on the whitelist").formatted(Formatting.DARK_RED) },
                                    true
                                )
                            }
                            0
                        }
                    )
                ).then(
                    literal("list")
                        .executes { context ->
                            val players = SimpleWhitelist.databaseManager.players
                            context.source.sendFeedback(
                                { Text.literal("Players on the whitelist: ${players.joinToString(", ")}").formatted(Formatting.GOLD) },
                                false
                            )
                            0
                        }
                ).then(
                    literal("on")
                        .executes { context ->
                            SimpleWhitelist.configManager.setWhitelist(true)
                            context.source.sendFeedback(
                                { Text.literal("Whitelist is enabled").formatted(Formatting.GREEN) },
                                true
                            )
                            0
                        }
                ).then(
                    literal("off")
                        .executes { context ->
                            SimpleWhitelist.configManager.setWhitelist(false)
                            context.source.sendFeedback(
                                { Text.literal("Whitelist is disabled").formatted(Formatting.RED) },
                                true
                            )
                            0
                        }
                )
        )
        dispatcher.register(literal("swh").redirect(command))
        dispatcher.register(literal("swl").redirect(command))
        dispatcher.register(literal("swhitelist").redirect(command))
    }
}