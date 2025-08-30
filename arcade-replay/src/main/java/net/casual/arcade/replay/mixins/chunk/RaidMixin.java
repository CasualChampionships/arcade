/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.chunk;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecordable;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders;
import net.casual.arcade.utils.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(Raid.class)
public class RaidMixin {
    @Shadow
    private BlockPos center;

    @Shadow
    @Final
    private ServerBossEvent raidEvent;

    @Inject(
        method = "updatePlayers",
        at = @At("TAIL")
    )
    private void onUpdate(ServerLevel level, CallbackInfo ci) {
        BoundingBox box = MathUtils.createBoundingBox(this.center, 96);
        ReplayChunkRecorders.updateRecordable((ReplayChunkRecordable) this.raidEvent, level.dimension(), box);
    }

    @Inject(
        method = "playSound",
        at = @At("TAIL")
    )
    private void onPlayerSound(ServerLevel level, BlockPos pos, CallbackInfo ci, @Local(ordinal = 0) long seed) {
        Collection<ReplayChunkRecorder> recorders = ((ReplayChunkRecordable) this.raidEvent).getRecorders();
        if (!recorders.isEmpty()) {
            ClientboundSoundPacket packet = new ClientboundSoundPacket(
                SoundEvents.RAID_HORN,
                SoundSource.NEUTRAL,
                this.center.getX(),
                this.center.getY(),
                this.center.getZ(),
                64,
                1.0F,
                seed
            );
            for (ReplayChunkRecorder recorder : recorders) {
                recorder.record(packet);
            }
        }
    }
}
