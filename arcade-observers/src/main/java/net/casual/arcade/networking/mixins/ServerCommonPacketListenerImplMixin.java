/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.networking.mixins;

import net.casual.arcade.events.GlobalEventHandler;
import net.casual.arcade.networking.events.ObserverClientboundPacketEvent;
import net.casual.arcade.networking.utils.ObserverUtilsKt;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 999)
public class ServerCommonPacketListenerImplMixin {
    @ModifyVariable(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
        at = @At("HEAD"),
        argsOnly = true,
        name = "packet"
    )
    private Packet<?> onSendPacket(Packet<?> packet) {
        ServerCommonPacketListenerImpl self = (ServerCommonPacketListenerImpl) (Object) this;
        if (self instanceof ServerGamePacketListenerImpl connection) {
            ObserverClientboundPacketEvent event = new ObserverClientboundPacketEvent(ObserverUtilsKt.asObserver(connection.player), packet);
            GlobalEventHandler.Server.broadcast(event);
            return event.getPacket();
        }
        return packet;
    }
}
