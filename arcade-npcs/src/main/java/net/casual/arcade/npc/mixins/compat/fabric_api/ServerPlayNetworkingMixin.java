/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.mixins.compat.fabric_api;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.npc.FakePlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworking.class)
public class ServerPlayNetworkingMixin {
    @ModifyReturnValue(
        method = {
            "canSend(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$Type;)Z",
            "canSend(Lnet/minecraft/server/network/ServerGamePacketListenerImpl;Lnet/minecraft/resources/Identifier;)Z"
        },
        at = @At("RETURN")
    )
    private static boolean returnTrueForFakePlayers(boolean original, ServerGamePacketListenerImpl connection) {
        return original || connection.player instanceof FakePlayer;
    }
}
