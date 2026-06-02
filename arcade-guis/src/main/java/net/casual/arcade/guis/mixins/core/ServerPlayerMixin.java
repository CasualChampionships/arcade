/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.mixins.core;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.guis.core.Gui;
import net.casual.arcade.guis.menu.GuiMenu;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Definition(id = "send", method = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V")
    @Definition(id = "ClientboundOpenScreenPacket", type = ClientboundOpenScreenPacket.class)
    @Expression("?.send(new ClientboundOpenScreenPacket(?, ?, ?))")
    @Inject(
        method = "openMenu",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private void onOpenContainerMenu(
        MenuProvider provider,
        CallbackInfoReturnable<OptionalInt> cir,
        @Local(name = "menu") AbstractContainerMenu menu
    ) {
        if (menu instanceof GuiMenu<?> guiMenu) {
            guiMenu.getGui().onOpen();
        }
    }

    @WrapOperation(
        method = "openMenu",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"
        )
    )
    private void markGuiClosingDueToReplaced(ServerPlayer instance, Operation<Void> original, MenuProvider provider) {
        Gui replacement = provider instanceof GuiMenu.Provider<?> guiProvider ? guiProvider.getGui() : null;
        ScopedValue.where(GuiMenu.CLOSE_REASON, new Gui.CloseReason.Replaced(replacement))
            .run(() -> original.call(instance));
    }

    @WrapOperation(
        method = {"tick", "doTick"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"
        )
    )
    private void markGuiClosingDueToInvalid(ServerPlayer instance, Operation<Void> original) {
        ScopedValue.where(GuiMenu.CLOSE_REASON, Gui.CloseReason.Invalid.INSTANCE)
            .run(() -> original.call(instance));
    }

    @WrapWithCondition(
        method = "closeContainer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private boolean shouldSendClosingPacket(ServerGamePacketListenerImpl instance, Packet<?> packet) {
        Gui.CloseReason reason = GuiMenu.CLOSE_REASON.orElse(Gui.CloseReason.Unknown.INSTANCE);
        if (reason instanceof Gui.CloseReason.Replaced replaced) {
            Gui gui = replaced.getGui();
            return gui == null || gui.shouldResetMousePosition();
        }
        return true;
    }
}
