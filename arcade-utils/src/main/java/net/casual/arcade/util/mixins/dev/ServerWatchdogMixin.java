/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins.dev;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.dedicated.ServerWatchdog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWatchdog.class)
public class ServerWatchdogMixin {
    @Inject(
        method = "run",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ceaseWatchdogInDevelopment(CallbackInfo ci) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            ci.cancel();
        }
    }
}
