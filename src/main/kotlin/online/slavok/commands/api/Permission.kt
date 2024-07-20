package online.slavok.commands.api

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.command.ServerCommandSource
import java.util.*
import java.util.function.Predicate


object Permission {
    private fun getSafe(
        predicate: Predicate<ServerCommandSource>,
        defaultRequiredLevel: Int
    ): Predicate<ServerCommandSource> {
        return if (FabricLoader.getInstance()
                .isModLoaded("fabric-permissions-api-v0")
        ) predicate else Predicate<ServerCommandSource> { source ->
            source.hasPermissionLevel(
                defaultRequiredLevel
            )
        }
    }

    fun require(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> {
        Objects.requireNonNull(permission, "permission")
        return getSafe(
            { source -> SafePermissionApi.check(source, permission, defaultRequiredLevel) },
            defaultRequiredLevel
        )
    }
}