/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.common;

import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Inject(
        method = "broadcast",
        at = @At("HEAD")
    )
    private void onBroadcast(Entity entity, Packet<?> packet, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            ReplayPlayerRecorders.record(player, packet);
        }
    }
}
