/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.interceptor.mixins;

import io.netty.channel.ChannelPipeline;
import net.casual.arcade.interceptor.ArcadeInterceptors;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {
    @Inject(
        method = "configureSerialization",
        at = @At("TAIL")
    )
    private static void addHttpHandlers(
        ChannelPipeline pipeline,
        PacketFlow inboundDirection,
        boolean local,
        BandwidthDebugMonitor monitor,
        CallbackInfo ci
    ) {
        if (inboundDirection == PacketFlow.SERVERBOUND) {
            ArcadeInterceptors.getHandlers().forEach(pipeline::addFirst);
        }
    }
}
