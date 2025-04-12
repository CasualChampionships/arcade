/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.casual.arcade.util.ducks.ConnectionFaultHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Nullable @Shadow private volatile PacketListener packetListener;

    @Inject(
        method = "exceptionCaught",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;disconnect(Lnet/minecraft/network/chat/Component;)V"
        )
    )
    private void onTimeoutDisconnect(
        ChannelHandlerContext channelHandlerContext,
        Throwable throwable,
        CallbackInfo ci
    ) {
        if (this.packetListener instanceof ServerCommonPacketListenerImpl connection) {
            ((ConnectionFaultHolder) connection).arcade$setTimedOut(true);
        }
    }

    @Inject(
        method = "exceptionCaught",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/network/Connection;packetListener:Lnet/minecraft/network/PacketListener;"
        )
    )
    private void onDisconnect(
        ChannelHandlerContext channelHandlerContext,
        Throwable throwable,
        CallbackInfo ci
    ) {
        if (this.packetListener instanceof ServerCommonPacketListenerImpl connection) {
            ((ConnectionFaultHolder) connection).arcade$setPacketError(throwable);
        }
    }
}
