//? if <1.22 {
package online.slavok.whitelist.commands.api

import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.command.CommandSource

// Excluded on 26+: fabric-permissions-api is not available there and the >=1.22
// branch of Permission gates on the vanilla command permission instead.
object SafePermissionApi {
    fun check(source: CommandSource, permission: String, defaultRequiredLevel: Int): Boolean {
        return Permissions.check(source, permission, defaultRequiredLevel)
    }
}
//?}
