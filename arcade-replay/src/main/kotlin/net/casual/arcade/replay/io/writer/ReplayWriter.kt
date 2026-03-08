/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer

import io.netty.buffer.ByteBuf
import io.netty.handler.codec.EncoderException
import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.replay.events.ReplayRecorderCloseEvent
import net.casual.arcade.replay.events.ReplayRecorderSaveEvent
import net.casual.arcade.replay.mixins.network.IdDispatchCodecAccessor
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.recorder.packet.RecordablePayload
import net.casual.arcade.replay.util.ReplayMarker
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.getDebugName
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.PacketType
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.CommonPacketTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.fileSize
import kotlin.time.Duration

public interface ReplayWriter {
    public val recorder: ReplayRecorder
    public val path: Path
    public val closed: Boolean

    public val markers: Int
        get() = 0

    public val cacheChunksOnUnload: Boolean
        get() = false

    public fun tick() {

    }

    public fun pause() {

    }

    public fun resume() {

    }

    public fun beginInitialization() {

    }

    public fun endInitialization() {

    }

    public fun canRecordPacket(packet: Packet<*>): Boolean {
        return true
    }

    public fun writePacket(
        packet: Packet<*>,
        protocol: ProtocolInfo<*>,
        timestamp: Duration,
        offThread: Boolean
    ): CompletableFuture<Int?>

    public fun writePlayer(player: ServerPlayer, packets: Collection<Packet<*>>) {
        for (packet in packets) {
            this.recorder.record(packet)
        }
    }

    public fun writeCachedChunk(pos: ChunkPos): Boolean {
        return false
    }

    public fun writeMarker(marker: ReplayMarker) {

    }

    public fun getRawRecordingSize(): Long

    public fun getOutputPath(): Path

    public fun close(duration: Duration, save: Boolean): CompletableFuture<Long>

    public companion object {
        private val EXECUTOR_NUMBER = AtomicInteger()

        public const val ENTRY_ARCADE_REPLAY_META: String = "arcade_replay_meta.json"

        public val ReplayWriter.name: String
            get() = this.recorder.getName()

        public fun createExecutor(): ExecutorService {
            return Executors.newSingleThreadExecutor { task ->
                val thread = Thread(task, "replay-writer-${EXECUTOR_NUMBER.incrementAndGet()}")
                thread.setUncaughtExceptionHandler { _, e ->
                    ArcadeUtils.logger.error("Uncaught exception while writing replay", e)
                }
                thread
            }
        }

        public fun encodePacket(packet: Packet<*>, protocol: ProtocolInfo<*>, buf: FriendlyByteBuf) {
            @Suppress("UNCHECKED_CAST")
            val codec = (protocol.codec() as StreamCodec<ByteBuf, Packet<*>>)

            if (packet is ClientboundCustomPayloadPacket) {
                val payload = packet.payload
                if (payload is RecordablePayload) {
                    @Suppress("UNCHECKED_CAST")
                    codec as IdDispatchCodecAccessor<PacketType<*>>

                    val id = codec.typeToIdMap.getInt(CommonPacketTypes.CLIENTBOUND_CUSTOM_PAYLOAD)
                    buf.writeVarInt(id)
                    buf.writeIdentifier(payload.type().id)
                    payload.record(buf)
                    return
                }
            }

            codec.encode(buf, packet)
        }

        internal fun ReplayWriter.close(
            save: Boolean,
            writer: () -> Unit,
            closer: () -> Unit
        ): Long {
            var size = 0L
            try {
                if (save) {
                    val output = this.getOutputPath()
                    val event = ReplayRecorderSaveEvent(this.recorder, output)
                    GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.PRE_PHASES)
                    writer.invoke()
                    size = output.fileSize()

                    GlobalEventHandler.Server.broadcast(event, BuiltInEventPhases.POST_PHASES)
                }
                try {
                    closer.invoke()
                    GlobalEventHandler.Server.broadcast(ReplayRecorderCloseEvent(this.recorder))
                } catch (exception: Exception) {
                    ArcadeUtils.logger.error("Failed to close replay writer", exception)
                }
            } catch (exception: Exception) {
                ArcadeUtils.logger.error("Failed to write replay ${this.name}", exception)
                throw exception
            }
            return size
        }

        internal fun handleLoggingEncoderException(
            logger: Logger,
            packet: Packet<*>,
            protocol: ProtocolInfo<*>,
            offThread: Boolean,
            exception: EncoderException
        ) {
            val name = packet.getDebugName()
            if (!offThread) {
                logger.error("Failed to encode packet $name, skipping", exception)
            } else {
                logger.error("Failed to encode packet $name during ${protocol.id()} likely due to being off-thread, skipping", exception)
            }
        }
    }
}