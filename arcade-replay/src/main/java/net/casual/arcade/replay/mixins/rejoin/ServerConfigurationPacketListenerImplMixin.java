/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.rejoin;

import net.casual.arcade.replay.recorder.rejoin.RejoinConfigurationPacketListener;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public class ServerConfigurationPacketListenerImplMixin {
    @Inject(
        method = "startNextTask",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onStartNextTask(CallbackInfo ci) {
        if ((Object) this instanceof RejoinConfigurationPacketListener) {
            ci.cancel();
        }
    }
}
