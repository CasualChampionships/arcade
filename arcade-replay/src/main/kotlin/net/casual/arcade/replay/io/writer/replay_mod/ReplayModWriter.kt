/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.replay_mod

import com.google.common.hash.Hashing
import com.google.gson.JsonObject
import com.replaymod.replaystudio.data.Marker
import com.replaymod.replaystudio.io.ReplayOutputStream
import com.replaymod.replaystudio.lib.viaversion.api.protocol.packet.State
import com.replaymod.replaystudio.lib.viaversion.api.protocol.version.ProtocolVersion
import com.replaymod.replaystudio.protocol.PacketTypeRegistry
import com.replaymod.replaystudio.replay.ReplayMetaData
import io.netty.buffer.Unpooled
import io.netty.handler.codec.EncoderException
import net.casual.arcade.replay.io.ReplayModIO
import net.casual.arcade.replay.io.writer.ReplayWriter
import net.casual.arcade.replay.io.writer.ReplayWriter.Companion.close
import net.casual.arcade.replay.io.writer.ReplayWriter.Companion.encodePacket
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.util.FileUtils
import net.casual.arcade.replay.util.ReplayMarker
import net.casual.arcade.replay.util.io.SizedZipReplayFile
import net.casual.arcade.utils.*
import net.minecraft.SharedConstants
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.world.entity.EntityType
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*
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

    private val packs = HashMap<Int, String>()
    private var packId = 0

    override var markers: Int = 0
    override val closed: Boolean
        get() = this.executor.isShutdown

    override fun canRecordPacket(packet: Packet<*>): Boolean {
        return when (packet) {
            is ClientboundResourcePackPushPacket -> this.downloadAndRecordResourcePack(packet)
            else -> true
        }
    }

    override fun writePacket(
        packet: Packet<*>,
        protocol: ProtocolInfo<*>,
        timestamp: Duration,
        offThread: Boolean
    ): CompletableFuture<Int?> {
        if (packet is ClientboundAddEntityPacket) {
            if (packet.type == EntityType.PLAYER) {
                val uuids = this.meta.players.toMutableSet()
                uuids.add(packet.uuid.toString())
                this.meta.players = uuids.toTypedArray()
                this.saveMeta()
            }
        }

        return CompletableFuture.supplyAsync({
            this.writePacketSync(packet, protocol, timestamp, offThread)
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
            val name = packet.getDebugName()
            if (!offThread) {
                ArcadeUtils.logger.error("Failed to encode packet $name, skipping", e)
                return null
            }
            ArcadeUtils.logger.error(
                "Failed to encode packet $name during ${protocol.id()} likely due to being off-thread, skipping", e
            )
            return null
        }
        val bytes = saved.buf.readableBytes()

        try {
            this.output.write(timestamp.inWholeMilliseconds, saved)
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to write packet", e)
        }
        return bytes
    }

    private fun encodePacket(packet: Packet<*>, protocol: ProtocolInfo<*>): ReplayPacket {
        val version = ProtocolVersion.getProtocol(SharedConstants.getProtocolVersion())
        val registry = PacketTypeRegistry.get(version, this.protocolAsState(protocol))

        val friendly = FriendlyByteBuf(Unpooled.buffer())
        try {
            encodePacket(packet, protocol, friendly)
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

    private fun downloadAndRecordResourcePack(packet: ClientboundResourcePackPushPacket): Boolean {
        if (!this.recorder.settings.includeResourcePacks || packet.url.startsWith("replay://")) {
            return true
        }
        @Suppress("DEPRECATION")
        val pathHash = Hashing.sha1().hashString(packet.url, StandardCharsets.UTF_8).toString()
        val path = ArcadeUtils.path.resolve("replays").resolve("packs").resolve(pathHash)

        val requestId = this.packId++
        if (!path.exists() || !this.writeResourcePack(path.readBytes(), packet.hash, requestId)) {
            CompletableFuture.runAsync {
                path.parent.createDirectories()
                val bytes = URI(packet.url).toURL().openStream().readAllBytes()
                path.writeBytes(bytes)
                if (!this.writeResourcePack(bytes, packet.hash, requestId)) {
                    ArcadeUtils.logger.error("Resource pack hashes do not match! Pack '${packet.url}' will not be loaded...")
                }
            }.exceptionally {
                ArcadeUtils.logger.error("Failed to download resource pack", it)
                null
            }
        }
        this.executor.execute {
            this.packs[requestId] = packet.url
        }
        this.recorder.record(ClientboundResourcePackPushPacket(
            packet.id,
            "replay://${requestId}",
            "",
            packet.required,
            packet.prompt
        ))
        return false
    }

    private fun writeResourcePack(bytes: ByteArray, expectedHash: String, id: Int): Boolean {
        @Suppress("DEPRECATION")
        val packHash = Hashing.sha1().hashBytes(bytes).toString()
        if (expectedHash == "" || expectedHash == packHash) {
            this.executor.execute {
                try {
                    val index = this.replay.resourcePackIndex ?: HashMap()
                    val write = !index.containsValue(packHash)
                    index[id] = packHash
                    this.replay.writeResourcePackIndex(index)
                    if (write) {
                        this.replay.writeResourcePack(packHash).use {
                            it.write(bytes)
                        }
                    }
                } catch (e: IOException) {
                    ArcadeUtils.logger.warn("Failed to write resource pack", e)
                }
            }
            return true
        }
        return false
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

                JsonUtils.encode(meta, it)
            }

            this.replay.write(ENTRY_SERVER_REPLAY_PACKS).writer().use {
                JsonUtils.encode(this.meta, it)
            }
        }
    }

    public companion object {
        private const val ENTRY_SERVER_REPLAY_PACKS = "server_replay_packs.json"

        public fun dated(recordings: Path): (ReplayRecorder) -> ReplayModWriter {
            val date = DateTimeUtils.getFormattedDate()
            return { ReplayModWriter(it, FileUtils.findNextAvailable(recordings.resolve(date))) }
        }
    }
}