/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.player;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
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

@Mixin(ChunkMap.TrackedEntity.class)
public class TrackedEntityMixin {
    @Shadow @Final private Entity entity;
    @Shadow @Final private ServerEntity serverEntity;

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    @SuppressWarnings("NameDoesntMatchTargetClass")
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
        method = {"sendToTrackingPlayers", "sendToTrackingPlayersFiltered"},
        at = @At("HEAD")
    )
    private void onBroadcast(CallbackInfo ci, @Local(name = "packet", argsOnly = true) Packet<? super ClientGamePacketListener> packet) {
        if (this.entity instanceof ServerPlayer player) {
            ReplayPlayerRecorders.record(player, packet);
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
