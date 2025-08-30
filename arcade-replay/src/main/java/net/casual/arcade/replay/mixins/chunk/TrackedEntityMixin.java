/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.chunk;

import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecordable;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ChunkMap.TrackedEntity.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public class TrackedEntityMixin implements ReplayChunkRecordable {
    @Unique
    private final Set<ReplayChunkRecorder> replay$chunks = new HashSet<>();

    @Shadow
    @Final
    Entity entity;
    @Shadow
    @Final
    ServerEntity serverEntity;

    @Inject(
        method = "broadcast",
        at = @At("HEAD")
    )
    private void onBroadcast(Packet<?> packet, CallbackInfo ci) {
        for (ReplayChunkRecorder recorder : this.replay$chunks) {
            recorder.record(packet);
        }
    }

    @Inject(
        method = "updatePlayers",
        at = @At("HEAD")
    )
    private void onUpdate(List<ServerPlayer> playersList, CallbackInfo ci) {
        ChunkPos pos = this.entity.chunkPosition();
        ResourceKey<Level> level = this.entity.level().dimension();
        ReplayChunkRecorders.updateRecordable(this, level, pos);
    }

    @Inject(
        method = "broadcastRemoved",
        at = @At("HEAD")
    )
    private void onRemoved(CallbackInfo ci) {
        this.removeAllRecorders();
    }

    @NotNull
    @Override
    public Collection<ReplayChunkRecorder> getRecorders() {
        return this.replay$chunks;
    }

    @Override
    public void addRecorder(@NotNull ReplayChunkRecorder recorder) {
        if (this.replay$chunks.add(recorder)) {
            recorder.addRecordable(this);
            List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();
            this.serverEntity.sendPairingData(recorder.getDummyPlayer(), list::add);
            recorder.record(new ClientboundBundlePacket(list));

            recorder.onEntityTracked(this.entity);
        }
    }

    @Override
    public void resendPackets(@NotNull ReplayChunkRecorder recorder) {
        List<Packet<? super ClientGamePacketListener>> list = new ArrayList<>();
        this.serverEntity.sendPairingData(recorder.getDummyPlayer(), list::add);
        recorder.record(new ClientboundBundlePacket(list));
    }

    @Override
    public void removeRecorder(@NotNull ReplayChunkRecorder recorder) {
        if (this.replay$chunks.remove(recorder)) {
            recorder.onEntityUntracked(this.entity);

            recorder.record(new ClientboundRemoveEntitiesPacket(
                this.entity.getId()
            ));
            recorder.removeRecordable(this);
        }
    }

    @Override
    public void removeAllRecorders() {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(this.entity.getId());
        for (ReplayChunkRecorder recorder : this.replay$chunks) {
            recorder.onEntityUntracked(this.entity);

            recorder.record(packet);
            recorder.removeRecordable(this);
        }
        this.replay$chunks.clear();
    }
}
