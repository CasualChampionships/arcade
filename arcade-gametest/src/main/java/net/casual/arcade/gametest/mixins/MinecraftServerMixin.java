/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.mixins;

import net.casual.arcade.gametest.ArcadeGametest;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(
        method = "sendSystemMessage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void checkForSuppressedSystemMessages(Component message, CallbackInfo ci) {
        if (ArcadeGametest.getSuppressSystemMessages()) {
            ci.cancel();
        }
    }
}
