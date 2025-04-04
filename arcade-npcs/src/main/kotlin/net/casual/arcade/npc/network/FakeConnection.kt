package net.casual.arcade.npc.network

import io.netty.channel.embedded.EmbeddedChannel
import net.casual.arcade.npc.mixins.ConnectionAccessor
import net.minecraft.network.Connection
import net.minecraft.network.PacketListener
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.PacketFlow

@Suppress("CAST_NEVER_SUCCEEDS")
public class FakeConnection: Connection(PacketFlow.SERVERBOUND) {
    init {
        (this as ConnectionAccessor).setChannel(EmbeddedChannel())
    }

    override fun <T: PacketListener?> setupInboundProtocol(protocolInfo: ProtocolInfo<T>, listener: T) {
        (this as ConnectionAccessor).setPacketListener(listener)
    }
}