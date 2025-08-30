/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.chunk;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecordable;
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Mixin(ServerBossEvent.class)
@SuppressWarnings("AddedMixinMembersNamePattern")
public abstract class ServerBossEventMixin extends BossEvent implements ReplayChunkRecordable {
    @Shadow
    private boolean visible;
    @Unique
    private final Set<ReplayChunkRecorder> replay$recorders = new HashSet<>();

    public ServerBossEventMixin(UUID id, Component name, BossBarColor color, BossBarOverlay overlay) {
        super(id, name, color, overlay);
    }

    @Inject(
        method = "broadcast",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"
        )
    )
    private void onBroadcast(
        Function<BossEvent, ClientboundBossEventPacket> packetGetter,
        CallbackInfo ci,
        @Local ClientboundBossEventPacket packet
    ) {
        for (ReplayChunkRecorder recorder : this.replay$recorders) {
            recorder.record(packet);
        }
    }

    @Inject(
        method = "removeAllPlayers",
        at = @At("TAIL")
    )
    private void onRemoveAll(CallbackInfo ci) {
        this.removeAllRecorders();
    }

    @Inject(
        method = "setVisible",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"
        )
    )
    private void onSetVisible(boolean visible, CallbackInfo ci) {
        ClientboundBossEventPacket packet = visible ?
            ClientboundBossEventPacket.createAddPacket(this) :
            ClientboundBossEventPacket.createRemovePacket(this.getId());
        for (ReplayChunkRecorder recorder : this.replay$recorders) {
            recorder.record(packet);
        }
    }

    @NotNull
    @Override
    public Collection<ReplayChunkRecorder> getRecorders() {
        return this.replay$recorders;
    }

    @Override
    public void addRecorder(@NotNull ReplayChunkRecorder recorder) {
        if (this.replay$recorders.add(recorder) && this.visible) {
            recorder.record(ClientboundBossEventPacket.createAddPacket(this));
            recorder.addRecordable(this);
        }
    }

    @Override
    public void resendPackets(@NotNull ReplayChunkRecorder recorder) {
        if (this.visible) {
            recorder.record(ClientboundBossEventPacket.createAddPacket(this));
        }
    }

    @Override
    public void removeRecorder(@NotNull ReplayChunkRecorder recorder) {
        if (this.replay$recorders.remove(recorder) && this.visible) {
            recorder.record(ClientboundBossEventPacket.createRemovePacket(this.getId()));
            recorder.removeRecordable(this);
        }
    }

    @Override
    public void removeAllRecorders() {
        if (this.visible) {
            ClientboundBossEventPacket packet = ClientboundBossEventPacket.createRemovePacket(this.getId());
            for (ReplayChunkRecorder recorder : this.replay$recorders) {
                recorder.record(packet);
            }
        }
        for (ReplayChunkRecorder recorder : this.replay$recorders) {
            recorder.removeRecordable(this);
        }
        this.replay$recorders.clear();
    }
}
