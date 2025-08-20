/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.events.server.player.PlayerServerboundPacketEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
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
    private static <T extends PacketListener> boolean shouldHandlePacket(Packet<T> instance, T listener) {
        if (listener instanceof ServerGamePacketListenerImpl connection) {
            PlayerServerboundPacketEvent event = new PlayerServerboundPacketEvent(connection.player, instance);
            GlobalEventHandler.Server.broadcast(event);
            return !event.isCancelled();
        }
        return true;
    }
}
