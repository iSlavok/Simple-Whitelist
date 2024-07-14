package online.slavok.commands.suggestionProviders

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.ServerCommandSource
import java.util.concurrent.CompletableFuture


class PlayerSuggestionProvider : SuggestionProvider<ServerCommandSource> {
    @Throws(CommandSyntaxException::class)
    override fun getSuggestions(
        context: CommandContext<ServerCommandSource>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val source = context.source
        val playerNames = source.playerNames
        for (playerName in playerNames) {
            builder.suggest(playerName)
        }
        return builder.buildFuture()
    }
}