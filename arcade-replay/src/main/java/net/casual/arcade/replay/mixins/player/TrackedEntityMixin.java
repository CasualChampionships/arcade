/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.player;

import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(ChunkMap.TrackedEntity.class)
public class TrackedEntityMixin {
    @Shadow
    @Final
    Entity entity;
    @Shadow
    @Final
    ServerEntity serverEntity;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void onCreated(
        ChunkMap chunkMap,
        Entity entity,
        int range,
        int updateInterval,
        boolean trackDelta,
        CallbackInfo ci
    ) {
        if (entity instanceof ServerPlayer player) {
            for (ReplayPlayerRecorder recorder : ReplayPlayerRecorders.get(player)) {
                recorder.spawnPlayer(this.serverEntity);
            }
        }
    }

    @Inject(
        method = "broadcastRemoved",
        at = @At("TAIL")
    )
    private void onRemoved(CallbackInfo ci) {
        if (this.entity instanceof ServerPlayer player) {
            for (ReplayPlayerRecorder recorder : ReplayPlayerRecorders.get(player)) {
                recorder.removePlayer(player);
            }
        }
    }
}
