/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(
        method = "handleInteract",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;setShiftKeyDown(Z)V",
            shift = At.Shift.AFTER
        ),
        cancellable = true
    )
    private void tryInteractWithVirtualEntity(
        ServerboundInteractPacket packet,
        CallbackInfo ci,
        @Local @Nullable Entity entity
    ) {
        if (entity == null) {
            PlayerAttachmentObserverExtension extension = PlayerAttachmentObserverExtension.getAttachmentObserver(this.player);
            if (extension.tryInteractWithVirtualEntity(packet)) {
                ci.cancel();
            }
        }
    }
}
