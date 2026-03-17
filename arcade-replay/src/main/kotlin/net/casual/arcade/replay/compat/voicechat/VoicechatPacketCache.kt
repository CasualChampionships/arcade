/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.voicechat

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import de.maxhenkel.voicechat.api.Position
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.audio.AudioConverter
import de.maxhenkel.voicechat.api.opus.OpusDecoder
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket
import net.casual.arcade.replay.io.ReplayFormat
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.concurrent.TimeUnit

internal class VoicechatPacketCache {
    private val channels: Cache<UUID, OpusDecoder> = this.createDecoderCache()
    private val players: Cache<UUID, OpusDecoder> = this.createDecoderCache()

    private val decoded = WeakHashMap<ByteArray, ShortArray>()

    fun getOrCreate(
        api: VoicechatApi,
        format: ReplayFormat,
        packet: LocationalSoundPacket
    ): Packet<ClientCommonPacketListener> {
        val decoded = this.decodeForChannel(api, packet.channelId, packet.opusEncodedData)
        return format.encoder().locational(api.audioConverter, packet.sender, decoded, packet.position, packet.distance)
    }

    fun getOrCreate(
        api: VoicechatApi,
        format: ReplayFormat,
        packet: EntitySoundPacket
    ): Packet<ClientCommonPacketListener> {
        val decoded = this.decodeForChannel(api, packet.channelId, packet.opusEncodedData)
        return format.encoder().entity(api.audioConverter, packet.sender, decoded, packet.isWhispering, packet.distance)
    }

    fun getOrCreate(
        api: VoicechatApi,
        format: ReplayFormat,
        packet: StaticSoundPacket
    ): Packet<ClientCommonPacketListener> {
        val decoded = this.decodeForChannel(api, packet.channelId, packet.opusEncodedData)
        return format.encoder().static(api.audioConverter, packet.sender, decoded)
    }

    fun create(
        api: VoicechatApi,
        format: ReplayFormat,
        encoded: ByteArray,
        sender: UUID,
        grouped: Boolean,
        whispering: Boolean,
        distance: Float
    ): Packet<ClientCommonPacketListener> {
        val decoded = this.decodeForPlayer(api, sender, encoded)
        val encoder = format.encoder()
        if (grouped) {
            return encoder.static(api.audioConverter, sender, decoded)
        }
        return encoder.entity(api.audioConverter, sender, decoded, whispering, distance)
    }

    fun cleanUp() {
        this.channels.cleanUp()
        this.players.cleanUp()
    }

    private fun decodeForChannel(api: VoicechatApi, channel: UUID, data: ByteArray): ShortArray {
        return this.decoded.getOrPut(data) {
            val decoder = this.channels.get(channel, api::createDecoder)
            decoder.decode(data)
        }
    }

    private fun decodeForPlayer(api: VoicechatApi, player: UUID, data: ByteArray): ShortArray {
        return this.decoded.getOrPut(data) {
            val decoder = this.players.get(player, api::createDecoder)
            decoder.decode(data)
        }
    }

    private fun createDecoderCache(): Cache<UUID, OpusDecoder> {
        return CacheBuilder.newBuilder()
            .removalListener<UUID, OpusDecoder> { it.value?.close() }
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build()
    }

    private fun ReplayFormat.encoder(): Encoder {
        return when (this) {
            ReplayFormat.ReplayMod -> ReplayModEncoder
            ReplayFormat.Flashback -> FlashbackEncoder
        }
    }

    private interface Encoder {
        fun locational(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener>

        fun entity(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener>

        fun static(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray
        ): Packet<ClientCommonPacketListener>
    }

    private object ReplayModEncoder: Encoder {
        /**
         * Packet version for the voicechat mod, see [here](https://github.com/henkelmax/replay-voice-chat/blob/master/src/main/java/de/maxhenkel/replayvoicechat/net/AbstractSoundPacket.java#L10).
         */
        const val VERSION: Int = 1

        override fun locational(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(VoicechatPayload.REPLAY_MOD_LOCATIONAL_TYPE, sender, decoded, converter) {
                writeDouble(position.x)
                writeDouble(position.y)
                writeDouble(position.z)
                writeFloat(distance)
            }
        }

        override fun entity(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(VoicechatPayload.REPLAY_MOD_ENTITY_TYPE, sender, decoded, converter) {
                writeBoolean(whispering)
                writeFloat(distance)
            }
        }

        override fun static(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray
        ): Packet<ClientCommonPacketListener> {
            return this.create(VoicechatPayload.REPLAY_MOD_STATIC_TYPE, sender, decoded, converter)
        }

        private fun create(
            type: CustomPacketPayload.Type<*>,
            sender: UUID,
            decoded: ShortArray,
            converter: AudioConverter,
            additional: FriendlyByteBuf.() -> Unit = { }
        ): Packet<ClientCommonPacketListener> {
            val raw = converter.shortsToBytes(decoded)
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
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray,
            position: Position,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(LOCATIONAL_SOUND, sender, decoded) {
                Vec3.STREAM_CODEC.encode(this, Vec3(position.x, position.y, position.z))
                writeFloat(distance)
            }
        }

        override fun entity(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray,
            whispering: Boolean,
            distance: Float
        ): Packet<ClientCommonPacketListener> {
            return this.create(ENTITY_SOUND, sender, decoded) {
                writeBoolean(whispering)
                writeFloat(distance)
            }
        }

        override fun static(
            converter: AudioConverter,
            sender: UUID,
            decoded: ShortArray
        ): Packet<ClientCommonPacketListener> {
            return this.create(STATIC_SOUND, sender, decoded)
        }

        private fun create(
            type: Int,
            sender: UUID,
            decoded: ShortArray,
            additional: FriendlyByteBuf.() -> Unit = { }
        ): ClientboundCustomPayloadPacket {
            val payload = VoicechatPayload.of(VoicechatPayload.FLASHBACK_TYPE) { buf ->
                buf.writeUUID(sender)
                buf.writeVarInt(decoded.size)
                for (sample in decoded) {
                    buf.writeShort(sample.toInt())
                }
                buf.writeByte(type)
                additional(buf)
            }
            return ClientboundCustomPayloadPacket(payload)
        }
    }
}