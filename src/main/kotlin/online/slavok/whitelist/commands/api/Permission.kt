package online.slavok.whitelist.commands.api

//? if >=1.22 {
/*import net.minecraft.commands.CommandSourceStack as ServerCommandSource
import net.minecraft.server.permissions.Permissions
import java.util.function.Predicate

// Minecraft 26 replaced integer operator levels with named command permissions,
// and fabric-permissions-api isn't published for 26+ yet, so gate on the vanilla
// command permission matching the requested operator level.
object Permission {
    fun require(permission: String, defaultRequiredLevel: Int): Predicate<ServerCommandSource> {
        val required = when {
            defaultRequiredLevel >= 4 -> Permissions.COMMANDS_OWNER
            defaultRequiredLevel == 3 -> Permissions.COMMANDS_ADMIN
            defaultRequiredLevel == 2 -> Permissions.COMMANDS_GAMEMASTER
            else -> Permissions.COMMANDS_MODERATOR
        }
        return Predicate { source -> source.permissions().hasPermission(required) }
    }
}*/
//?} else {
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
//?}
