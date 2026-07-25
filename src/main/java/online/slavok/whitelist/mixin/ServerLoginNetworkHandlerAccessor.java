package online.slavok.whitelist.mixin;

import com.mojang.authlib.GameProfile;
//? if >=1.22 {
/*import net.minecraft.server.network.ServerLoginPacketListenerImpl;*/
//?} else {
import net.minecraft.server.network.ServerLoginNetworkHandler;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// 26+ (unobfuscated) renamed ServerLoginNetworkHandler -> ServerLoginPacketListenerImpl
// and the GameProfile field profile -> authenticatedProfile.
//? if >=1.22 {
/*@Mixin(ServerLoginPacketListenerImpl.class)
public interface ServerLoginNetworkHandlerAccessor {
    @Accessor("authenticatedProfile") GameProfile getProfile();
}*/
//?} else {
@Mixin(ServerLoginNetworkHandler.class)
public interface ServerLoginNetworkHandlerAccessor {
    @Accessor GameProfile getProfile();
}
//?}
