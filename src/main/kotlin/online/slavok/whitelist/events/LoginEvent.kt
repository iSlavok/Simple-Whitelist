package online.slavok.whitelist.events

import com.mojang.authlib.GameProfile
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import online.slavok.whitelist.SimpleWhitelist
import online.slavok.whitelist.mixin.ServerLoginNetworkHandlerAccessor
class LoginEvent {
    fun register() {
        ServerLoginConnectionEvents.QUERY_START.register(
            ServerLoginConnectionEvents.QueryStart { handler, _, _, _ ->
                val accessor = handler as ServerLoginNetworkHandlerAccessor
                val profile: GameProfile = accessor.getProfile()
                val playerName = profile.name
                if (SimpleWhitelist.configManager.config.whitelist && !SimpleWhitelist.databaseManager.inWhitelist(playerName)) {
                    handler.disconnect(Text.literal("You are not on the whitelist!").formatted(Formatting.RED))
                }
            }
        )
    }
}