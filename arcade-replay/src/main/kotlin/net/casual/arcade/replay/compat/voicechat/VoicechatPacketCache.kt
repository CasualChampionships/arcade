/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.voicechat

import de.maxhenkel.voicechat.api.Position
import de.maxhenkel.voicechat.api.audio.AudioConverter
import de.maxhenkel.voicechat.api.opus.OpusDecoder
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket
import de.maxhenkel.voicechat.api.packets.MicrophonePacket
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.utils.EnumUtils
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import java.util.*
import de.maxhenkel.voicechat.api.packets.Packet as VoicechatPacket

public class VoicechatPacketCache {
    private val universe = EnumUtils.mapOf<ReplayFormat, WeakHashMap<VoicechatPacket, Packet<ClientCommonPacketListener>>>()

    init {
        for (format in ReplayFormat.entries) {
            this.universe[format] = WeakHashMap()
        }
    }

    public fun getOrCreate(
        format: ReplayFormat,
        decoder: OpusDecoder,
        converter: AudioConverter,
        packet: LocationalSoundPacket
    ): Packet<ClientCommonPacketListener> {
        return this.universe[format]!!.getOrPut(packet) {
            format.encoder().locational(
                decoder, converter, packet.sender, packet.opusEncodedData, packet.position, packet.distance
            )
        }
    }

    public fun getOrCreate(
        format: ReplayFormat,
        decoder: OpusDecoder,
        converter: AudioConverter,
        packet: EntitySoundPacket
    ): Packet<ClientCommonPacketListener> {
        return this.universe[format]!!.getOrPut(packet) {
            format.encoder().entity(
                decoder, converter, packet.sender, packet.opusEncodedData, packet.isWhispering, packet.distance
            )
        }
    }

    public fun getOrCreate(
        format: ReplayFormat,
        decoder: OpusDecoder,
        converter: AudioConverter,
        packet: StaticSoundPacket
    ): Packet<ClientCommonPacketListener> {
        return this.universe[format]!!.getOrPut(packet) {
            format.encoder().static(decoder, converter, packet.sender, packet.opusEncodedData)
        }
    }

    public fun create(
        format: ReplayFormat,
        decoder: OpusDecoder,
        converter: AudioConverter,
        packet: MicrophonePacket,
        sender: UUID,
        grouped: Boolean,
        distance: Float
    ): Packet<ClientCommonPacketListener> {
        val encoder = format.encoder()
        if (grouped) {
            return encoder.static(decoder, converter, sender, packet.opusEncodedData)
        }
        return encoder.entity(decoder, converter, sender, packet.opusEncodedData, packet.isWhispering, distance)
    }

    private fun ReplayFormat.encoder(): Encoder {
        return when (this) {
            ReplayFormat.ReplayMod -> ReplayModEncoder
            ReplayFormat.Flashback -> FlashbackEncoder
        }
    }

    private interface Encoder {
        fun locational(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener>

        fun entity(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener>

        fun static(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray
        ): Packet<ClientCommonPacketListener>
    }

    private object ReplayModEncoder: Encoder {
        /**
         * Packet version for the voicechat mod, see [here](https://github.com/henkelmax/replay-voice-chat/blob/master/src/main/java/de/maxhenkel/replayvoicechat/net/AbstractSoundPacket.java#L10).
         */
        const val VERSION: Int = 1

        override fun locational(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(VoicechatPayload.REPLAY_MOD_LOCATIONAL_TYPE, sender, data, decoder, converter) {
                writeDouble(position.x)
                writeDouble(position.y)
                writeDouble(position.z)
                writeFloat(distance)
            }
        }

        override fun entity(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(VoicechatPayload.REPLAY_MOD_ENTITY_TYPE, sender, data, decoder, converter) {
                writeBoolean(whispering)
                writeFloat(distance)
            }
        }

        override fun static(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray
        ): Packet<ClientCommonPacketListener> {
            return this.create(VoicechatPayload.REPLAY_MOD_STATIC_TYPE, sender, data, decoder, converter)
        }

        private fun create(
            type: CustomPacketPayload.Type<*>,
            sender: UUID,
            encoded: ByteArray,
            decoder: OpusDecoder,
            converter: AudioConverter,
            additional: FriendlyByteBuf.() -> Unit = { }
        ): Packet<ClientCommonPacketListener> {
            // We are forced to decode on the server-side since replay-voice-chat
            // reads the raw packet data when it reads the replay.
            val raw = converter.shortsToBytes(decoder.decode(encoded))
            val payload = VoicechatPayload.of(type) { buf ->
                buf.writeShort(VERSION)
                buf.writeUUID(sender)
                buf.writeByteArray(raw)
                additional(buf)
            }
            return ClientboundCustomPayloadPacket(payload)
        }
    }

    private object FlashbackEncoder: Encoder {
        const val STATIC_SOUND = 0
        const val LOCATIONAL_SOUND = 1
        const val ENTITY_SOUND = 2

        override fun locational(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(LOCATIONAL_SOUND, sender, data, decoder) {
                writeVec3(Vec3(position.x, position.y, position.z))
                writeFloat(distance)
            }
        }

        override fun entity(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(ENTITY_SOUND, sender, data, decoder) {
                writeBoolean(whispering)
                writeFloat(distance)
            }
        }

        override fun static(
            decoder: OpusDecoder,
            converter: AudioConverter,
            sender: UUID,
            data: ByteArray
        ): Packet<ClientCommonPacketListener> {
            return this.create(STATIC_SOUND, sender, data, decoder)
        }

        private fun create(
            type: Int,
            sender: UUID,
            encoded: ByteArray,
            decoder: OpusDecoder,
            additional: FriendlyByteBuf.() -> Unit = { }
        ): ClientboundCustomPayloadPacket {
            val raw = decoder.decode(encoded)
            val payload = VoicechatPayload.of(VoicechatPayload.FLASHBACK_TYPE) { buf ->
                buf.writeUUID(sender)
                buf.writeVarInt(raw.size)
                for (sample in raw) {
                    buf.writeShort(sample.toInt())
                }
                buf.writeByte(type)
                additional(buf)
            }
            return ClientboundCustomPayloadPacket(payload)
        }
    }
}