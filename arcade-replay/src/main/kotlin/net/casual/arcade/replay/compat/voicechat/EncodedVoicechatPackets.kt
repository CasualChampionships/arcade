/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.voicechat

import de.maxhenkel.voicechat.api.Position
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket
import net.casual.arcade.replay.io.ReplayFormat
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.world.phys.Vec3
import java.util.UUID

public object EncodedVoicechatPackets {
    public fun get(
        format: ReplayFormat,
        packet: LocationalSoundPacket
    ): Packet<ClientCommonPacketListener> {
        return format.encoder().locational(packet.sender, packet.opusEncodedData, packet.position, packet.distance)
    }

    public fun get(
        format: ReplayFormat,
        packet: EntitySoundPacket
    ): Packet<ClientCommonPacketListener> {
        return format.encoder().entity(packet.sender, packet.opusEncodedData, packet.isWhispering, packet.distance)
    }

    public fun get(
        format: ReplayFormat,
        packet: StaticSoundPacket
    ): Packet<ClientCommonPacketListener> {
        return format.encoder().static(packet.sender, packet.opusEncodedData)
    }

    public fun create(
        format: ReplayFormat,
        data: ByteArray,
        sender: UUID,
        grouped: Boolean,
        whispering: Boolean,
        distance: Float
    ): Packet<ClientCommonPacketListener> {
        val encoder = format.encoder()
        if (grouped) {
            return encoder.static(sender, data)
        }
        return encoder.entity(sender, data, whispering, distance)
    }

    private fun ReplayFormat.encoder(): Encoder {
        return when (this) {
            ReplayFormat.Flashback -> FlashbackEncoder
            ReplayFormat.ReplayMod -> throw IllegalStateException("Recording encoded voice data " +
                    "is not supported for the ReplayMod format")
        }
    }

    private interface Encoder {
        fun locational(
            sender: UUID,
            data: ByteArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener>

        fun entity(
            sender: UUID,
            data: ByteArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener>

        fun static(
            sender: UUID,
            data: ByteArray,
        ): Packet<ClientCommonPacketListener>
    }

    private object FlashbackEncoder: Encoder {
        const val STATIC_SOUND = 0
        const val LOCATIONAL_SOUND = 1
        const val ENTITY_SOUND = 2

        override fun locational(
            sender: UUID,
            data: ByteArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(LOCATIONAL_SOUND, sender, data) {
                writeVec3(Vec3(position.x, position.y, position.z))
                writeFloat(distance)
            }
        }

        override fun entity(
            sender: UUID,
            data: ByteArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(ENTITY_SOUND, sender, data) {
                writeBoolean(whispering)
                writeFloat(distance)
            }
        }

        override fun static(
            sender: UUID,
            data: ByteArray
        ): Packet<ClientCommonPacketListener> {
            return this.create(STATIC_SOUND, sender, data)
        }

        private fun create(
            type: Int,
            sender: UUID,
            data: ByteArray,
            additional: FriendlyByteBuf.() -> Unit = { }
        ): ClientboundCustomPayloadPacket {
            val payload = VoicechatPayload.of(VoicechatPayload.ENCODED_FLASHBACK_TYPE) { buf ->
                buf.writeUUID(sender)
                buf.writeByteArray(data)
                buf.writeByte(type)
                additional(buf)
            }
            return ClientboundCustomPayloadPacket(payload)
        }
    }
}