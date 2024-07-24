package online.slavok.whitelist.commands.suggestionProviders

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import online.slavok.whitelist.SimpleWhitelist
import java.util.concurrent.CompletableFuture

class WhitelistPlayerSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    @Throws(CommandSyntaxException::class)
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val playerNames = SimpleWhitelist.databaseManager.getAll()
        for (playerName in playerNames) {
            builder.suggest(playerName)
        }
        return builder.buildFuture()
    }
}