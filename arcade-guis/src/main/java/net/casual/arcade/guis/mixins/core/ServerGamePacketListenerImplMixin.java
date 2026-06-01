/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.guis.core.Gui;
import net.casual.arcade.guis.menu.ContainerGuiMenu;
import net.casual.arcade.guis.menu.GuiMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @Shadow public ServerPlayer player;

    @ModifyExpressionValue(
        method = "handleContainerClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"
        )
    )
    private boolean overrideSpectatorClickCheck(boolean original) {
        if (this.player.containerMenu instanceof ContainerGuiMenu menu) {
            if (menu.getGui().getCanSpectatorsClick()) {
                // If spectators *can* click we treat them as not spectators
                return false;
            }
        }
        return original;
    }

    @WrapOperation(
        method = "handleContainerClose",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;doCloseContainer()V"
        )
    )
    private void markGuiClosingDueToPlayer(ServerPlayer instance, Operation<Void> original) {
        ScopedValue.where(GuiMenu.CLOSE_REASON, Gui.CloseReason.Player.INSTANCE)
            .run(() -> original.call(instance));
    }
}
