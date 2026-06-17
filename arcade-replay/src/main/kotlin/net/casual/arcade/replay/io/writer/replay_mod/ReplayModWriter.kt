/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.replay_mod

import com.google.gson.JsonObject
import com.replaymod.replaystudio.data.Marker
import com.replaymod.replaystudio.io.ReplayOutputStream
import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State
import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion
import com.replaymod.replaystudio.protocol.PacketTypeRegistry
import com.replaymod.replaystudio.replay.ReplayMetaData
import io.netty.buffer.Unpooled
import io.netty.handler.codec.EncoderException
import kotlinx.atomicfu.atomic
import net.casual.arcade.replay.io.ReplayModIO
import net.casual.arcade.replay.io.writer.ReplayWriter
import net.casual.arcade.replay.io.writer.ReplayWriter.Companion.close
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.util.FileUtils
import net.casual.arcade.replay.util.ReplayMarker
import net.casual.arcade.replay.util.io.ResourcePackCache
import net.casual.arcade.replay.util.io.SizedZipReplayFile
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.DateTimeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.toByteArray
import net.minecraft.SharedConstants
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.EntityTypes
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.name
import kotlin.time.Duration
import com.github.steveice10.netty.buffer.Unpooled as ReplayUnpooled
import com.replaymod.replaystudio.protocol.Packet as ReplayPacket

public class ReplayModWriter(
    override val recorder: ReplayRecorder,
    override val path: Path
): ReplayWriter {
    private val executor = ReplayWriter.createExecutor()

    private val replay: SizedZipReplayFile = SizedZipReplayFile(out = this.path.toFile())
    private val output: ReplayOutputStream = this.replay.writePacketData()
    private val meta: ReplayMetaData = this.createNewMeta()

    private val resourcePackId = atomic(0)

    override var markers: Int = 0
    override val closed: Boolean
        get() = this.executor.isShutdown

    override fun writePacket(
        packet: Packet<*>,
        protocol: ProtocolInfo<*>,
        timestamp: Duration,
        offThread: Boolean
    ): CompletableFuture<Int?> {
        if (packet is ClientboundAddEntityPacket) {
            if (packet.type == EntityTypes.PLAYER) {
                val uuids = this.meta.players.toMutableSet()
                uuids.add(packet.uuid.toString())
                this.meta.players = uuids.toTypedArray()
                this.saveMeta()
            }
        }

        val replacement = when (packet) {
            is ClientboundResourcePackPushPacket -> this.downloadAndRecordResourcePack(packet)
            else -> packet
        }

        return CompletableFuture.supplyAsync({
            this.writePacketSync(replacement, protocol, timestamp, offThread)
        }, this.executor)
    }

    override fun writeMarker(marker: ReplayMarker) {
        this.markers++
        val instance = Marker()
        instance.time = marker.timestamp.inWholeMilliseconds.toInt()
        instance.name = marker.name
        if (marker.position != null) {
            instance.x = marker.position.x
            instance.y = marker.position.y
            instance.z = marker.position.z
        }
        if (marker.rotation != null) {
            instance.pitch = marker.rotation.x
            instance.yaw = marker.rotation.y
        }
        this.executor.execute {
            val markers = this.replay.markers.or(::HashSet)
            markers.add(instance)
            this.replay.writeMarkers(markers)
        }
    }

    override fun getRawRecordingSize(): Long {
        return this.replay.getRawFileSize()
    }

    override fun getOutputPath(): Path {
        return this.path.parent.resolve(this.path.name + ".mcpr")
    }

    override fun close(duration: Duration, save: Boolean): CompletableFuture<Long> {
        if (save) {
            this.meta.duration = duration.inWholeMilliseconds.toInt()
            this.saveMeta()
        }
        val future = CompletableFuture.supplyAsync({
            fun write() {
                this.replay.saveTo(this.getOutputPath().toFile())
            }
            fun close() {
                this.replay.close()
                ReplayModIO.deleteCaches(this.path)
            }
            this.close(save, ::write, ::close)
        }, this.executor)

        this.executor.shutdown()
        return future
    }

    private fun writePacketSync(
        packet: Packet<*>,
        protocol: ProtocolInfo<*>,
        timestamp: Duration,
        offThread: Boolean
    ): Int? {
        val saved = try {
            this.encodePacket(packet, protocol)
        } catch (e: EncoderException) {
            ReplayWriter.handleLoggingEncoderException(LOGGER, packet, protocol, offThread, e)
            return null
        }
        val bytes = saved.buf.readableBytes()

        try {
            this.output.write(timestamp.inWholeMilliseconds, saved)
        } catch (e: IOException) {
            LOGGER.error("Failed to write packet", e)
        }
        return bytes
    }

    private fun encodePacket(packet: Packet<*>, protocol: ProtocolInfo<*>): ReplayPacket {
        val version = ProtocolVersion.getProtocol(SharedConstants.getProtocolVersion())
        val registry = PacketTypeRegistry.get(version, this.protocolAsState(protocol))

        val friendly = FriendlyByteBuf(Unpooled.buffer())
        try {
            ReplayWriter.encodePacket(packet, protocol, friendly, this.recorder.getPacketContextProvider())
            val id = friendly.readVarInt()
            return ReplayPacket(registry, id, ReplayUnpooled.wrappedBuffer(friendly.toByteArray()))
        } finally {
            friendly.release()
        }
    }

    private fun protocolAsState(protocol: ProtocolInfo<*>): State {
        return when (protocol.id()) {
            ConnectionProtocol.PLAY -> State.PLAY
            ConnectionProtocol.CONFIGURATION -> State.CONFIGURATION
            ConnectionProtocol.LOGIN -> State.LOGIN
            else -> throw IllegalStateException("Expected connection protocol to be 'PLAY', 'CONFIGURATION' or 'LOGIN'")
        }
    }

    private fun downloadAndRecordResourcePack(
        packet: ClientboundResourcePackPushPacket
    ): ClientboundResourcePackPushPacket {
        if (!this.recorder.settings.includeResourcePacks) {
            return packet
        }

        val packId = this.resourcePackId.getAndIncrement()
        val expectedHash = packet.hash
        ResourcePackCache.get(packet.url, expectedHash).thenAcceptAsync({ bytes ->
            this.writeResourcePack(bytes, expectedHash, packId)
        }, this.executor)
        return ClientboundResourcePackPushPacket(
            packet.id, "replay://$packId", "", packet.required, packet.prompt
        )
    }

    private fun writeResourcePack(bytes: ByteArray, expectedHash: String, id: Int) {
        val realHash = ResourcePackCache.hash(bytes)
        if (expectedHash != "" && expectedHash != realHash) {
            return
        }

        try {
            val index = this.replay.resourcePackIndex ?: HashMap()
            if (!index.containsValue(realHash)) {
                this.replay.writeResourcePack(realHash).use { it.write(bytes) }
            }
            index[id] = realHash
            this.replay.writeResourcePackIndex(index)
        } catch (e: IOException) {
            LOGGER.warn("Failed to write resource pack", e)
        }
    }

    private fun createNewMeta(): ReplayMetaData {
        val meta = ReplayMetaData()
        meta.isSingleplayer = false
        meta.serverName = this.recorder.settings.worldName
        meta.customServerName = this.recorder.settings.serverName
        meta.generator = "ArcadeReplay v${ArcadeUtils.version}"
        meta.date = System.currentTimeMillis()
        meta.mcVersion = SharedConstants.getCurrentVersion().name()
        meta.fileFormatVersion = ReplayMetaData.CURRENT_FILE_FORMAT_VERSION
        meta.setProtocolVersion(SharedConstants.getProtocolVersion())
        return meta
    }

    private fun saveMeta() {
        this.executor.execute {
            this.replay.writeMetaData(null, this.meta)

            this.replay.write(ReplayWriter.ENTRY_ARCADE_REPLAY_META).writer().use {
                val meta = JsonObject()
                this.recorder.addMetadata(meta)

                JsonUtils.encodeRaw(meta, it)
            }
        }
    }

    public companion object {
        private val LOGGER = LoggerFactory.getLogger("replay-mod-writer")

        public fun dated(recordings: Path): (ReplayRecorder) -> ReplayModWriter {
            val date = DateTimeUtils.getFormattedDate()
            return { ReplayModWriter(it, FileUtils.findNextAvailable(recordings.resolve(date))) }
        }
    }
}