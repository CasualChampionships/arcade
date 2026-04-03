/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.clock;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.dimensions.level.extensions.LevelClockExtension;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ServerClockManager.class)
public class ServerClockManagerMixin {
    @Shadow private MinecraftServer server;

    @WrapOperation(
        method = "modifyClock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private void syncCustomClocks(
        PlayerList instance,
        Packet<?> packet,
        Operation<Void> original,
        @Local(name = "clock") Holder<WorldClock> clock
    ) {
        if (!LevelClockExtension.shouldManuallySyncTime(this.server)) {
            original.call(instance, packet);
            return;
        }

        for (ServerPlayer player : instance.getPlayers()) {
            ServerLevel level = player.level();
            Optional<ResourceKey<WorldClock>> defaultKey = level.dimensionType().defaultClock()
                .flatMap(Holder::unwrapKey);
            if (defaultKey.isPresent() && clock.is(defaultKey.get())) {
                LevelClockExtension extension = LevelClockExtension.getClockExtension(level);
                if (!extension.customized()) {
                    player.connection.send(packet);
                }
            }
        }
    }
}
