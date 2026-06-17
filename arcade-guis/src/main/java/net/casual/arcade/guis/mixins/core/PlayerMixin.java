/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.mixins.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.guis.core.Gui;
import net.casual.arcade.guis.menu.GuiMenu;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @WrapOperation(
        method = "remove",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;doCloseContainer()V"
        )
    )
    private void markGuiClosingDueToRemoval(Player instance, Operation<Void> original, Entity.RemovalReason reason) {
        ScopedValue.where(GuiMenu.CLOSE_REASON, new Gui.CloseReason.Removed(reason))
            .run(() -> original.call(instance));
    }
}
