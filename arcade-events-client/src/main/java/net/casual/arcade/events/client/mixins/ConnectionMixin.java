/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.client.network.ClientboundPacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Connection.class)
public class ConnectionMixin {
    @WrapWithCondition(
        method = "genericsFtw",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/Packet;handle(Lnet/minecraft/network/PacketListener;)V"
        )
    )
    private static <T extends PacketListener> boolean broadcastClientboundPacketEvent(Packet<T> instance, T listener) {
        if (listener instanceof ClientCommonPacketListenerImpl) {
            ClientboundPacketEvent event = new ClientboundPacketEvent(Minecraft.getInstance(), instance);
            GlobalEventHandler.Client.broadcast(event);
            return !event.isCancelled();
        }
        return true;
    }
}
