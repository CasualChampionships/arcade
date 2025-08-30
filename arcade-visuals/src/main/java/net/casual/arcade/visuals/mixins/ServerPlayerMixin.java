/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.mixins;

import net.casual.arcade.visuals.extensions.PlayerCameraExtension;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(
        method = "broadcastToPlayer",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onBroadcastToPlayer(ServerPlayer player, CallbackInfoReturnable<Boolean> cir) {
        PlayerCameraExtension extension = PlayerCameraExtension.getCameraExtension(player);
        if (extension.get() != null) {
            cir.setReturnValue(false);
        }
    }
}
