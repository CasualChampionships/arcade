/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.compat.c2me;

import com.ishland.c2me.rewrites.chunksystem.common.*;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.ItemStatus;
import com.ishland.flowsched.scheduler.StatusAdvancingScheduler;
import net.casual.arcade.replay.mixins.rejoin.ChunkMapAccessor;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecordable;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TheChunkSystem.class, remap = false)
public abstract class TheChunkSystemMixin extends StatusAdvancingScheduler<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> {
    @Shadow
    @Final
    private ChunkMap tacs;

    @Shadow
    protected abstract ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> getUnloadedStatus();

    @Inject(
        method = "onItemUpgrade",
        at = @At("HEAD")
    )
    private void onLoadChunk(
        ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder,
        ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> statusReached,
        CallbackInfo ci
    ) {
        ServerLevel level = ((ChunkMapAccessor) this.tacs).getLevel();
        level.getServer().execute(() -> {
            for (ReplayChunkRecorder recorder : ReplayChunkRecorders.containing(level.dimension(), holder.getKey())) {
                ((ReplayChunkRecordable) holder.getUserData().get()).addRecorder(recorder);
            }
        });
    }

    @Inject(
        method = "onItemDowngrade",
        at = @At("HEAD")
    )
    private void onUnloadChunk(
        ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder,
        ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> statusReached,
        CallbackInfo ci
    ) {
        if (((NewChunkStatus) statusReached).toChunkLevelType() == FullChunkStatus.INACCESSIBLE) {
            ServerLevel level = ((ChunkMapAccessor) this.tacs).getLevel();
            level.getServer().execute(() -> {
                ((ReplayChunkRecordable) holder.getUserData().get()).removeAllRecorders();
            });
        }
    }
}
