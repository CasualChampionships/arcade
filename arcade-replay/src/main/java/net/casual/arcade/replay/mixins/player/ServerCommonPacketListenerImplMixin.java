/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.mixins.player;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import net.casual.arcade.replay.ducks.ReplayViewable;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder;
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders;
import net.casual.arcade.replay.viewer.ReplayViewer;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// We want to apply our #send mixin *LAST*, any
// other mods which modify the packets should come first
@Mixin(value = ServerCommonPacketListenerImpl.class, priority = 5000)
public abstract class ServerCommonPacketListenerImplMixin {
    @Shadow
    protected abstract GameProfile playerProfile();

    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V"
        )
    )
    private void onPacket(Packet<?> packet, ChannelFutureListener sendListener, CallbackInfo ci) {
        ReplayPlayerRecorders.recordIfInitialized(this.playerProfile().id(), packet);
    }

    @Inject(
        method = "onDisconnect",
        at = @At("TAIL")
    )
    private void onDisconnect(DisconnectionDetails disconnectionDetails, CallbackInfo ci) {
        ReplayPlayerRecorders.stop(this.playerProfile().id());

        if (this instanceof ReplayViewable viewable) {
            ReplayViewer viewer = viewable.arcade$getViewingReplay();
            if (viewer != null) {
                viewer.close();
            }
        }
    }
}
