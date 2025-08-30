/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.casual.arcade.visuals.extensions.PlayerCameraExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyReturnValue(
        method = "isSpectator",
        at = @At("RETURN")
    )
    private boolean onIsSpectator(boolean original) {
        if ((Object) this instanceof ServerPlayer player) {
            PlayerCameraExtension extension = PlayerCameraExtension.getCameraExtension(player);
            if (extension.get() != null) {
                return true;
            }
        }
        return original;
    }
}
