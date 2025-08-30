/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.compat.carpet;

import carpet.patches.NetHandlerPlayServerFake;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayServerFake.class)
public class NetHandlerPlayServerFakeMixin extends ServerGamePacketListenerImpl {
    public NetHandlerPlayServerFakeMixin(MinecraftServer minecraftServer, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, serverPlayer, commonListenerCookie);
    }

    @Inject(
        method = "send",
        at = @At("HEAD")
    )
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        ReplayPlayerRecorders.record(this.player, packet);
    }
}
