/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import kotlin.Unit;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ServerboundFinishConfigurationPacket;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin {
    @Shadow
    @Final
    private GameProfile gameProfile;

    @WrapOperation(
        method = "handleSelectKnownPacks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/config/SynchronizeRegistriesTask;handleResponse(Ljava/util/List;Ljava/util/function/Consumer;)V"
        )
    )
    private void replacePacketsForRecorders(
        SynchronizeRegistriesTask instance,
        List<KnownPack> acceptedPacks,
        Consumer<Packet<?>> connection,
        Operation<Void> original
    ) {
        Collection<ReplayPlayerRecorder> recorders = ReplayPlayerRecorders.get(this.gameProfile.id());
        this.ignoreForAll(recorders.iterator(), () -> original.call(instance, acceptedPacks, connection));

        if (!recorders.isEmpty()) {
            // Replays don't negotiate "KnownPacks" so we need to record *all* the registries
            original.call(instance, List.of(), (Consumer<Packet<?>>) packet -> {
                for (ReplayPlayerRecorder recorder : recorders) {
                    recorder.record(packet);
                }
            });
        }
    }

    @Inject(
        method = "handleConfigurationFinished",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/config/PrepareSpawnTask;spawnPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/network/CommonListenerCookie;)Lnet/minecraft/server/level/ServerPlayer;"
        )
    )
    private void beforePlacePlayer(
        ServerboundFinishConfigurationPacket packet,
        CallbackInfo ci
    ) {
        Collection<ReplayPlayerRecorder> recorders = ReplayPlayerRecorders.get(this.gameProfile.id());
        recorders.forEach(ReplayPlayerRecorder::afterConfigure);
    }

    @Unique
    private void ignoreForAll(Iterator<ReplayPlayerRecorder> recorders, Runnable runnable) {
        if (!recorders.hasNext()) {
            runnable.run();
            return;
        }
        ReplayPlayerRecorder recorder = recorders.next();
        recorder.ignore(() -> {
            this.ignoreForAll(recorders, runnable);
            return Unit.INSTANCE;
        });
    }
}
