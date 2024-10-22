package online.slavok.whitelist.commands.api

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.command.CommandSource

object SafePermissionApi {
    fun check(source: CommandSource, permission: String, defaultRequiredLevel: Int): Boolean {
        return Permissions.check(source, permission, defaultRequiredLevel)
    }
}