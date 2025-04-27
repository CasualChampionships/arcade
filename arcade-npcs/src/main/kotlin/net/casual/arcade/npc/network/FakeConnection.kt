/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.network

import io.netty.channel.embedded.EmbeddedChannel
import net.casual.arcade.npc.mixins.ConnectionAccessor
import net.fabricmc.fabric.impl.networking.NetworkHandlerExtensions
import net.minecraft.network.Connection
import net.minecraft.network.PacketListener
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.PacketFlow

@Suppress("CAST_NEVER_SUCCEEDS")
public class FakeConnection: Connection(PacketFlow.SERVERBOUND) {
    init {
        (this as ConnectionAccessor).setChannel(EmbeddedChannel())
    }

    @Suppress("UnstableApiUsage")
    override fun <T: PacketListener?> setupInboundProtocol(protocolInfo: ProtocolInfo<T>, listener: T) {
        // Prevent memory leaks with fabric api
        val old = this.packetListener
        if (old is NetworkHandlerExtensions) {
            old.addon.endSession()
        }

        (this as ConnectionAccessor).setPacketListener(listener)
    }
}