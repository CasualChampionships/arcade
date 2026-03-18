/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.viewer

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.casual.arcade.replay.ducks.ReplayViewable
import net.casual.arcade.replay.mixins.viewer.ClientboundPlayerInfoUpdatePacketAccessor
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.configuration.ConfigurationProtocols
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.*
import com.replaymod.replaystudio.protocol.Packet as ReplayPacket

public object ReplayViewerUtils {
    public fun ReplayPacket.toClientboundPlayPacket(protocol: ProtocolInfo<ClientGamePacketListener>): Packet<*> {
        val wrapper = FriendlyByteBuf(Unpooled.buffer())
        useByteBuf(this.buf) { buf ->
            try {
                wrapper.writeVarInt(this.id)
                wrapper.writeBytes(buf)
                return protocol.codec().decode(wrapper)
            } finally {
                wrapper.release()
            }
        }
    }

    public fun ReplayPacket.toClientboundConfigurationPacket(): Packet<*> {
        val wrapper = FriendlyByteBuf(Unpooled.buffer())
        useByteBuf(this.buf) { buf ->
            try {
                wrapper.writeVarInt(this.id)
                wrapper.writeBytes(buf)
                return ConfigurationProtocols.CLIENTBOUND.codec().decode(wrapper)
            } finally {
                wrapper.release()
            }
        }
    }

    private inline fun <T> useByteBuf(buf: com.github.steveice10.netty.buffer.ByteBuf, block: (ByteBuf) -> T): T {
        // When we compile we map steveice10.netty -> io.netty
        // We just need this check for dev environment
        @Suppress("USELESS_IS_CHECK", "IMPOSSIBLE_IS_CHECK_WARNING")
        if (buf is ByteBuf) {
            return block(buf)
        }

        val array = ByteArray(buf.readableBytes())
        buf.readBytes(array)
        val copy = Unpooled.wrappedBuffer(array)
        try {
            return block(copy)
        } finally {
            copy.release()
        }
    }

    public fun ServerGamePacketListenerImpl.sendReplayPacket(packet: Packet<*>) {
        (this as ReplayViewable).`arcade$sendReplayViewerPacket`(packet)
    }

    public fun ServerGamePacketListenerImpl.startViewingReplay(viewer: ReplayViewer) {
        (this as ReplayViewable).`arcade$startViewingReplay`(viewer)
    }

    public fun ServerGamePacketListenerImpl.stopViewingReplay() {
        (this as ReplayViewable).`arcade$stopViewingReplay`()
    }

    public fun ServerGamePacketListenerImpl.getViewingReplay(): ReplayViewer? {
        return (this as ReplayViewable).`arcade$getViewingReplay`()
    }

    public fun createClientboundPlayerInfoUpdatePacket(
        actions: EnumSet<Action>,
        entries: List<Entry>
    ): ClientboundPlayerInfoUpdatePacket {
        val packet = ClientboundPlayerInfoUpdatePacket(actions, listOf())
        @Suppress("KotlinConstantConditions")
        (packet as ClientboundPlayerInfoUpdatePacketAccessor).setEntries(entries)
        return packet
    }
}