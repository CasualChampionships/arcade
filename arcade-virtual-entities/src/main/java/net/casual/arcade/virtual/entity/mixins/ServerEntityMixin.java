/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import net.casual.arcade.observer.utils.ObserverUtilsKt;
import net.casual.arcade.virtual.entity.extensions.EntityAttachmentExtension;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(
        method = "addPairing",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerEntity;sendPairingData(Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"
        )
    )
    private void startObservingEntityAttachments(ServerPlayer player, CallbackInfo ci) {
        EntityAttachmentExtension extension = EntityAttachmentExtension.getAttachmentExtension(this.entity);
        extension.startObserving(ObserverUtilsKt.asObserver(player));
    }

    @Inject(
        method = "sendPairingData",
        at = @At("TAIL")
    )
    private void sendEntityAttachmentPackets(
        ServerPlayer player,
        Consumer<Packet<ClientGamePacketListener>> broadcast,
        CallbackInfo ci
    ) {
        EntityAttachmentExtension extension = EntityAttachmentExtension.getAttachmentExtension(this.entity);
        extension.sendObservingSpawnPackets(ObserverUtilsKt.asObserver(player), broadcast);
    }

    @Inject(
        method = "removePairing",
        at = @At("HEAD")
    )
    private void removeObservingEntityAttachments(ServerPlayer player, CallbackInfo ci) {
        EntityAttachmentExtension extension = EntityAttachmentExtension.getAttachmentExtension(this.entity);
        extension.stopObserving(ObserverUtilsKt.asObserver(player));
    }
}
