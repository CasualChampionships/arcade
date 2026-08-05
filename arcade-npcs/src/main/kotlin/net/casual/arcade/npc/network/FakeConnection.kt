/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.network

import io.netty.channel.embedded.EmbeddedChannel
import net.casual.arcade.npc.mixins.ConnectionAccessor
import net.fabricmc.fabric.impl.networking.PacketListenerExtensions
import net.minecraft.network.Connection
import net.minecraft.network.PacketListener
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.PacketFlow
import org.jetbrains.annotations.ApiStatus.Internal

@Suppress("CAST_NEVER_SUCCEEDS")
public class FakeConnection: Connection(PacketFlow.SERVERBOUND) {
    @Internal
    public val embedded: EmbeddedChannel = EmbeddedChannel()

    init {
        (this as ConnectionAccessor).arcade_setChannel(this.embedded)
    }

    override fun <T: PacketListener> setupInboundProtocol(protocolInfo: ProtocolInfo<T>, listener: T) {
        // Prevent memory leaks with fabric api
        val old = this.packetListener

        @Suppress("UnstableApiUsage")
        if (old is PacketListenerExtensions) {
            old.addon.endSession()
        }

        (this as ConnectionAccessor).arcade_setPacketListener(listener)
    }
}