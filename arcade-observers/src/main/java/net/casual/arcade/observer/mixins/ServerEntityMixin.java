/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.observer.mixins;

import net.casual.arcade.observer.extensions.EntityObserversExtension;
import net.casual.arcade.observer.utils.ObserverUtilsKt;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
        EntityObserversExtension extension = EntityObserversExtension.getObserversExtension(this.entity);
        extension.startObserving(ObserverUtilsKt.asObserver(player));
    }

    @Inject(
        method = "removePairing",
        at = @At("HEAD")
    )
    private void removeObservingEntityAttachments(ServerPlayer player, CallbackInfo ci) {
        EntityObserversExtension extension = EntityObserversExtension.getObserversExtension(this.entity);
        extension.stopObserving(ObserverUtilsKt.asObserver(player));
    }
}
