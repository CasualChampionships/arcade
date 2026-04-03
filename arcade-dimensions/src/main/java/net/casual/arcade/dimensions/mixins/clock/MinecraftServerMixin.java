/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.clock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.dimensions.level.extensions.LevelClockExtension;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @WrapOperation(
        method = "onGameRuleChanged",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private void appendLevelClockTime(PlayerList instance, Packet<?> packet, Operation<Void> original) {
        if (!LevelClockExtension.shouldManuallySyncTime((MinecraftServer) (Object) this)) {
            original.call(instance, packet);
            return;
        }

        for (ServerPlayer player : instance.getPlayers()) {
            player.connection.send(
                LevelClockExtension.modifyClientboundSetTimePacket((ClientboundSetTimePacket) packet, player.level())
            );
        }
    }
}
