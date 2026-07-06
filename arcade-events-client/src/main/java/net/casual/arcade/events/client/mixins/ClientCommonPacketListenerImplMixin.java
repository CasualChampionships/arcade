/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.network.ServerboundPacketEvent;
import net.casual.arcade.events.phase.BuiltInEventPhases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
    @Shadow
    @Final
    protected Minecraft minecraft;

    @Shadow
    public abstract void send(Packet<?> packet);

    @ModifyVariable(
        method = "send",
        at = @At("HEAD"),
        argsOnly = true,
        name = "packet"
    )
    private Packet<?> broadcastSendPacketPre(Packet<?> packet, @Cancellable CallbackInfo ci) {
        ServerboundPacketEvent event = new ServerboundPacketEvent(this.minecraft, packet);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.PRE_PHASES_RAW);
        if (event.isCancelled()) {
            ci.cancel();
        }
        return event.getPacket();
    }

    @WrapOperation(
        method = "send",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private void broadcastSendPacketPost(Connection instance, Packet<?> packet, Operation<Void> original) {
        ServerboundPacketEvent event = new ServerboundPacketEvent(this.minecraft, packet);
        GlobalEventHandler.Client.broadcast(event, BuiltInEventPhases.POST_PHASES_RAW);
    }

    @WrapWithCondition(
        method = {"handleResourcePackPush", "handleRequestCookie"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    private boolean replaceRawSendCall(Connection instance, Packet<?> packet) {
        this.send(packet);
        return false;
    }
}
