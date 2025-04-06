/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.mixins;

import net.casual.arcade.host.ducks.MutableConnectionAddressHolder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerHandshakePacketListenerImpl.class)
public class ServerHandshakePacketListenerImplMixin {
    @Shadow @Final private Connection connection;

    @Inject(
        method = "beginLogin",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/Connection;setupInboundProtocol(Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/PacketListener;)V"
        )
    )
    private void onLogin(ClientIntentionPacket packet, boolean transferred, CallbackInfo ci) {
        ((MutableConnectionAddressHolder) this.connection).arcade$setConnectionAddress(packet.hostName(), packet.port());
    }
}
