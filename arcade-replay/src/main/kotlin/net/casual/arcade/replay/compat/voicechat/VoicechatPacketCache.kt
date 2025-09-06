/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.voicechat

import de.maxhenkel.voicechat.api.Position
import de.maxhenkel.voicechat.api.audio.AudioConverter
import de.maxhenkel.voicechat.api.packets.EntitySoundPacket
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket
import de.maxhenkel.voicechat.plugins.impl.packets.SoundPacketImpl
import de.maxhenkel.voicechat.voice.common.SoundPacket
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.utils.EnumUtils
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientCommonPacketListener
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.world.phys.Vec3
import java.util.*

public class VoicechatPacketCache {
    private val universe = EnumUtils.mapOf<ReplayFormat, WeakHashMap<SoundPacket<*>, Packet<ClientCommonPacketListener>>>()

    init {
        for (format in ReplayFormat.entries) {
            this.universe[format] = WeakHashMap()
        }
    }

    public fun getOrCreate(
        format: ReplayFormat,
        converter: AudioConverter,
        packet: LocationalSoundPacket,
        decoded: Lazy<ShortArray>
    ): Packet<ClientCommonPacketListener> {
        return this.universe[format]!!.getOrPut(packet.unwrap()) {
            format.encoder().locational(converter, packet.sender, decoded.value, packet.position, packet.distance)
        }
    }

    public fun getOrCreate(
        format: ReplayFormat,
        converter: AudioConverter,
        packet: EntitySoundPacket,
        decoded: Lazy<ShortArray>
    ): Packet<ClientCommonPacketListener> {
        return this.universe[format]!!.getOrPut(packet.unwrap()) {
            format.encoder().entity(converter, packet.sender, decoded.value, packet.isWhispering, packet.distance)
        }
    }

    public fun getOrCreate(
        format: ReplayFormat,
        converter: AudioConverter,
        packet: StaticSoundPacket,
        decoded: Lazy<ShortArray>
    ): Packet<ClientCommonPacketListener> {
        return this.universe[format]!!.getOrPut(packet.unwrap()) {
            format.encoder().static(converter, packet.sender, decoded.value)
        }
    }

    public fun create(
        format: ReplayFormat,
        converter: AudioConverter,
        decoded: ShortArray,
        sender: UUID,
        grouped: Boolean,
        whispering: Boolean,
        distance: Float
    ): Packet<ClientCommonPacketListener> {
        val encoder = format.encoder()
        if (grouped) {
            return encoder.static(converter, sender, decoded)
        }
        return encoder.entity(converter, sender, decoded, whispering, distance)
    }

    private fun de.maxhenkel.voicechat.api.packets.SoundPacket.unwrap(): SoundPacket<*> {
        return (this as SoundPacketImpl).packet
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
                writeVec3(Vec3(position.x, position.y, position.z))
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