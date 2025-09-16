/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.mixins.level;

import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.dimensions.level.CustomLevel;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @WrapOperation(
        method = "setViewDistance",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private void onBroadcastViewRadius(
        PlayerList instance,
        Packet<?> packet,
        Operation<Void> original
    ) {
        this.broadcastToAll(instance, packet, original, level -> level.getProperties().getViewDistance().isPresent());
    }

    @WrapOperation(
        method = "setSimulationDistance",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private void onBroadcastSimulationRadius(
        PlayerList instance,
        Packet<?> packet,
        Operation<Void> original
    ) {
        this.broadcastToAll(instance, packet, original, level -> level.getProperties().getSimulationDistance().isPresent());
    }

    @WrapWithCondition(
        method = "setViewDistance",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;setViewDistance(I)V"
        )
    )
    private boolean onSetChunkCacheViewDistance(ServerChunkCache instance, int viewDistance) {
        return !(instance.getLevel() instanceof CustomLevel custom) || custom.getProperties().getViewDistance().isEmpty();
    }

    @WrapWithCondition(
        method = "setSimulationDistance",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerChunkCache;setSimulationDistance(I)V"
        )
    )
    private boolean onSetChunkCacheSimulationDistance(ServerChunkCache instance, int simulationDistance) {
        return !(instance.getLevel() instanceof CustomLevel custom) || custom.getProperties().getSimulationDistance().isEmpty();
    }

    @Unique
    private void broadcastToAll(
        PlayerList players,
        Packet<?> packet,
        Operation<Void> original,
        Predicate<CustomLevel> hasCustomDistance
    ) {
        Iterable<ServerLevel> levels = players.getServer().getAllLevels();
        boolean anyHaveCustomDistance = Iterables.any(levels, level -> {
            return level instanceof CustomLevel custom && hasCustomDistance.test(custom);
        });
        if (!anyHaveCustomDistance) {
            original.call(players, packet);
            return;
        }

        for (ServerLevel level : levels) {
            if (!(level instanceof CustomLevel custom) || !hasCustomDistance.test(custom)) {
                players.broadcastAll(packet, level.dimension());
            }
        }
    }
}
