package net.casual.arcade.npc.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.casual.arcade.npc.FakePlayer;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Shadow @Final private MinecraftServer server;

    @WrapOperation(
        method = "placeNewPlayer",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/network/ServerGamePacketListenerImpl;"
        )
    )
    private ServerGamePacketListenerImpl onConstructGamePacketListener(
        MinecraftServer server,
        Connection connection,
        ServerPlayer player,
        CommonListenerCookie cookie,
        Operation<ServerGamePacketListenerImpl> original
    ) {
        if (player instanceof FakePlayer fake) {
            return fake.createConnection(server, connection, cookie);
        }
        return original.call(server, connection, player, cookie);
    }

    @WrapOperation(
        method = "respawn",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)Lnet/minecraft/server/level/ServerPlayer;"
        )
    )
    private ServerPlayer onConstructServerPlayer(
        MinecraftServer server,
        ServerLevel level,
        GameProfile profile,
        ClientInformation clientInformation,
        Operation<ServerPlayer> original,
        ServerPlayer previous
    ) {
        if (previous instanceof FakePlayer fake) {
            return fake.createRespawned(server, level, profile);
        }
        return original.call(server, level, profile, clientInformation);
    }
}
