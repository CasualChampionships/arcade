/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.chunk;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecordable;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static net.minecraft.server.level.ChunkHolder.UNLOADED_LEVEL_CHUNK;

@Mixin(ChunkHolder.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ChunkHolderMixin extends GenerationChunkHolder implements ReplayChunkRecordable {
    @Unique
    private final Set<ReplayChunkRecorder> replay$recorders = new HashSet<>();

    public ChunkHolderMixin(ChunkPos pos) {
        super(pos);
    }

    @Shadow
    public abstract CompletableFuture<ChunkResult<LevelChunk>> getFullChunkFuture();

    @Inject(
        method = "broadcast",
        at = @At("HEAD")
    )
    private void onBroadcast(List<ServerPlayer> players, Packet<?> packet, CallbackInfo ci) {
        for (ReplayChunkRecorder recorder : this.replay$recorders) {
            recorder.record(packet);
        }
    }

    @ModifyExpressionValue(
        method = "broadcastChanges",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/List;isEmpty()Z",
            remap = false
        )
    )
    private boolean shouldSkipBroadcasting(boolean noPlayers) {
        return noPlayers && this.replay$recorders.isEmpty();
    }

    @Inject(
        method = "updateFutures",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ChunkHolder;addSaveDependency(Ljava/util/concurrent/CompletableFuture;)V",
            shift = At.Shift.AFTER,
            ordinal = 0
        )
    )
    private void onChunkLoad(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        this.getFullChunkFuture().thenAccept(result -> {
            result.ifSuccess(chunk -> {
                for (ReplayChunkRecorder recorder : this.getRecorders()) {
                    recorder.onChunkLoaded(chunk);
                }
            });
        });
    }

    @Inject(
        method = "updateFutures",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;complete(Ljava/lang/Object;)Z",
            ordinal = 0
        )
    )
    private void onChunkUnload(ChunkMap chunkMap, Executor executor, CallbackInfo ci) {
        LevelChunk chunk = this.getFullChunk();
        if (chunk != null) {
            for (ReplayChunkRecorder recorder : this.getRecorders()) {
                recorder.onChunkUnloaded(this.pos, chunk);
            }
        }
    }

    @NotNull
    @Override
    public Collection<ReplayChunkRecorder> getRecorders() {
        return this.replay$recorders;
    }

    @Override
    public void addRecorder(@NotNull ReplayChunkRecorder recorder) {
        if (this.replay$recorders.add(recorder)) {
            this.getFullChunkFuture().thenAccept(result -> {
                result.ifSuccess(recorder::onChunkLoaded);
            });

            recorder.addRecordable(this);
        }
    }

    @Override
    public void resendPackets(@NotNull ReplayChunkRecorder recorder) {

    }

    @Override
    public void removeRecorder(@NotNull ReplayChunkRecorder recorder) {
        if (this.replay$recorders.remove(recorder)) {
            LevelChunk chunk = this.getFullChunk();
            if (chunk != null) {
                recorder.onChunkUnloaded(this.pos, chunk);
            }

            recorder.removeRecordable(this);
        }
    }

    @Override
    public void removeAllRecorders() {
        LevelChunk chunk = this.getFullChunk();
        for (ReplayChunkRecorder recorder : this.replay$recorders) {
            if (chunk != null) {
                recorder.onChunkUnloaded(this.pos, chunk);
            }
            recorder.removeRecordable(this);
        }
        this.replay$recorders.clear();
    }


    @Unique
    @Nullable
    private LevelChunk getFullChunk() {
        return this.getFullChunkFuture().getNow(UNLOADED_LEVEL_CHUNK).orElse(null);
    }
}
