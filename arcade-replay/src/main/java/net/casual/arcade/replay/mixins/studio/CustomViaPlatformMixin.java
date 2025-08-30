/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.studio;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.replaymod.replaystudio.viaversion.CustomViaPlatform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.logging.Logger;

@Mixin(value = CustomViaPlatform.class, remap = false)
public class CustomViaPlatformMixin {
    @ModifyReturnValue(
        method = "getLogger",
        at = @At("RETURN")
    )
    private Logger onGetLogger(Logger original) {
        // We don't care, shut up, please :)
        original.setFilter(log -> false);
        return original;
    }
}
