/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.flashback

import com.google.common.collect.HashMultimap
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.replay.compat.voicechat.VoicechatPayload
import net.casual.arcade.replay.io.FlashbackIO
import net.casual.arcade.replay.io.writer.ReplayWriter
import net.casual.arcade.replay.io.writer.ReplayWriter.Companion.close
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.util.FileUtils
import net.casual.arcade.replay.util.ReplayMarker
import net.casual.arcade.replay.util.flashback.FlashbackAction
import net.casual.arcade.replay.util.flashback.FlashbackMarker.Location
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.DateTimeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.getSpoofedOrRealDimension
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket
import net.minecraft.network.protocol.game.*
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import org.apache.commons.io.file.PathUtils
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.io.path.name
import kotlin.io.path.writer
import kotlin.time.Duration

public class FlashbackWriter(
    override val recorder: ReplayRecorder,
    override val path: Path
): ReplayWriter {
    private val executor = ReplayWriter.createExecutor()

    private val writer = FlashbackChunkedWriter(this.path, this.recorder.server.registryAccess(), this.recorder.settings)

    private val movement = HashMultimap.create<ResourceKey<Level>, EntityMovement>()
    private val chunks = Object2IntOpenHashMap<ChunkPacketIdentity>()
    private val recent = Object2ObjectOpenHashMap<ResourceKey<Level>, Long2IntOpenHashMap>()

    private val forcePlaySnapshot = AtomicBoolean(false)

    private var dimension: ResourceKey<Level>? = null

    private var ticksSinceLastSnapshot = 0
    private var ticks = 1

    override var markers: Int = 0

    override val cacheChunksOnUnload: Boolean
        get() = true

    override val closed: Boolean
        get() = this.executor.isShutdown

    init {
        this.chunks.defaultReturnValue(-1)
    }

    override fun tick() {
        this.writeEntityMovement()
        if (this.recorder.paused) {
            return
        }

        val previous = this.dimension
        this.dimension = this.recorder.level.dimension()

        this.writeActionAsync(FlashbackAction.NextTick)
        this.ticks++

        val ticks = this.ticks
        val chunkTicks = ticks - this.ticksSinceLastSnapshot
        if (chunkTicks >= FlashbackIO.CHUNK_LENGTH || (previous != null && previous != this.dimension)) {
            this.startNewReplayChunk()
        }
    }

    override fun resume() {
        this.startNewReplayChunk()
        this.executor.execute {
            this.forcePlaySnapshot.set(true)
        }
    }

    override fun beginInitialization() {
        this.executor.execute {
            this.writer.startSnapshot()
        }
    }

    override fun endInitialization() {
        this.executor.execute {
            this.writer.endSnapshot()
            this.writer.writeAction(FlashbackAction.NextTick)
        }
    }

    override fun canRecordPacket(packet: Packet<*>): Boolean {
        return !this.recorder.paused && !IGNORED_PACKETS.contains(packet::class.java)
    }

    override fun writePacket(
        packet: Packet<*>,
        protocol: ProtocolInfo<*>,
        timestamp: Duration,
        offThread: Boolean
    ): CompletableFuture<Int?> {
        val action = when (protocol.id()) {
            ConnectionProtocol.PLAY -> FlashbackAction.GamePacket
            ConnectionProtocol.CONFIGURATION -> FlashbackAction.ConfigurationPacket
            else -> return CompletableFuture.completedFuture(null)
        }

        val replacement = when (packet) {
            is ClientboundLevelChunkWithLightPacket -> return this.writeCachedChunk(packet, protocol)
            is ClientboundMoveEntityPacket -> return this.writeMovement(packet)
            is ClientboundCustomPayloadPacket -> when (val payload = packet.payload) {
                is VoicechatPayload -> return this.writeVoicechat(payload)
                else -> packet
            }
            else -> packet
        }

        return this.writeActionAsync(action) { buf ->
            val start = buf.writerIndex()
            ReplayWriter.encodePacket(replacement, protocol, buf)
            buf.writerIndex() - start
        }
    }

    override fun writePlayer(player: ServerPlayer, packets: Collection<Packet<*>>) {
        val uuid = player.uuid
        val position = player.position()
        val rotation = player.rotationVector
        val headRot = player.yHeadRot
        val velocity = player.deltaMovement
        val profile = player.gameProfile
        val gamemode = player.gameMode().id
        this.writeActionAsync(FlashbackAction.CreatePlayer) { buf ->
            buf.writeUUID(uuid)
            buf.writeDouble(position.x)
            buf.writeDouble(position.y)
            buf.writeDouble(position.z)
            buf.writeFloat(rotation.x)
            buf.writeFloat(rotation.y)
            buf.writeFloat(headRot)
            buf.writeVec3(velocity)
            ByteBufCodecs.GAME_PROFILE.encode(buf, profile)
            buf.writeVarInt(gamemode)
        }
        val filtered = packets.filter { it !is ClientboundAddEntityPacket }
        for (packet in filtered) {
            this.recorder.record(packet)
        }
    }

    override fun writeCachedChunk(pos: ChunkPos): Boolean {
        val dimension = this.recorder.level.dimension()
        val chunks = this.recent[dimension] ?: return false
        val posAsLong = pos.toLong()
        if (!chunks.containsKey(posAsLong)) {
            return false
        }
        val index = chunks.get(posAsLong)
        this.writeActionAsync(FlashbackAction.CacheChunk) { buf ->
            buf.writeVarInt(index)
        }
        return true
    }

    override fun writeMarker(marker: ReplayMarker) {
        this.markers++
        this.executor.execute {
            val location = Location.from(marker.position, this.recorder.level.dimension())
            this.writer.addMarker(this.ticks, marker.name, marker.color, location)
        }
    }

    override fun getRawRecordingSize(): Long {
        return PathUtils.sizeOf(this.path)
    }

    override fun getOutputPath(): Path {
        return this.path.parent.resolve(this.path.name + ".zip")
    }

    override fun close(duration: Duration, save: Boolean): CompletableFuture<Long> {
        val future = CompletableFuture.supplyAsync({
            fun write() {
                this.writer.endChunk(this.ticks, this.shouldForcePlaySnapshot())
                this.writeCustomMeta()
                FileUtils.zip(this.path, this.getOutputPath())
            }
            this.close(save, ::write, this.writer::close)
        }, this.executor)
        this.executor.shutdown()
        return future
    }

    private fun startNewReplayChunk() {
        val ticks = this.ticks
        this.ticksSinceLastSnapshot = ticks

        this.executor.execute {
            this.writer.endChunk(ticks, this.shouldForcePlaySnapshot())
            this.writer.startSnapshot()
        }
        this.recorder.takeSnapshot()
        this.executor.execute {
            this.writer.endSnapshot()
        }
    }

    private fun shouldForcePlaySnapshot(): Boolean {
        return this.forcePlaySnapshot.getAndSet(false)
    }

    private fun writeCachedChunk(
        packet: ClientboundLevelChunkWithLightPacket,
        protocol: ProtocolInfo<*>
    ): CompletableFuture<Int?> {
        val dimension = this.recorder.level.dimension()
        return this.writeActionAsync(FlashbackAction.CacheChunk) { buf ->
            val identity = ChunkPacketIdentity.of(packet)
            var index = this.chunks.getInt(identity)
            var size = -buf.writerIndex()
            if (index == -1) {
                index = this.chunks.size
                val fileIndex = FlashbackIO.getChunkCacheFileIndex(index)
                this.writer.writeLevelChunk(fileIndex) { chunkBuf ->
                    val start = chunkBuf.writerIndex()
                    ReplayWriter.encodePacket(packet, protocol, chunkBuf)
                    size += (chunkBuf.writerIndex() - start)
                }
                this.chunks.put(identity, index)
            }
            val map = this.recent.getOrPut(dimension, ::Long2IntOpenHashMap)
            map.put(ChunkPos.asLong(packet.x, packet.z), index)
            buf.writeVarInt(index)
            size + buf.writerIndex()
        }
    }

    @Suppress("SameParameterValue")
    private fun writeActionAsync(action: FlashbackAction) {
        this.executor.execute {
            this.writer.writeAction(action)
        }
    }

    private fun <T> writeActionAsync(
        action: FlashbackAction,
        block: (RegistryFriendlyByteBuf) -> T
    ): CompletableFuture<T> {
        return CompletableFuture.supplyAsync({
            this.writer.writeAction(action, block)
        }, this.executor).exceptionally { e ->
            ArcadeUtils.logger.error("Something went wrong writing action $action", e)
            null
        }
    }

    private fun writeCustomMeta() {
        try {
            val meta = JsonObject()
            this.recorder.addMetadata(meta)
            val path = this.path.resolve(ReplayWriter.ENTRY_ARCADE_REPLAY_META)
            path.writer().use { JsonUtils.encode(meta, it) }
        } catch (exception: Exception) {
            ArcadeUtils.logger.error("Failed to write ServerReplay meta!", exception)
        }
    }

    private fun writeEntityMovement() {
        this.executor.execute {
            if (this.movement.keySet().isNotEmpty()) {
                this.writer.writeAction(FlashbackAction.MoveEntities) { buf ->
                    buf.writeVarInt(this.movement.keySet().size)
                    for ((dimension, deltas) in this.movement.asMap()) {
                        buf.writeResourceKey(dimension)
                        buf.writeVarInt(deltas.size)
                        for (movement in deltas) {
                            movement.write(buf)
                        }
                    }
                    this.movement.clear()
                }
            }
        }
    }

    private fun writeMovement(packet: ClientboundMoveEntityPacket): CompletableFuture<Int?> {
        val level = this.recorder.level
        val entity = packet.getEntity(level) ?: return CompletableFuture.completedFuture(null)
        val id = entity.id
        val position = entity.position()
        val rotation = entity.rotationVector
        val headRot = entity.yHeadRot
        val onGround = entity.onGround()
        this.executor.execute {
            val movement = EntityMovement(id, position, rotation, headRot, onGround)
            this.movement.put(level.getSpoofedOrRealDimension(), movement)
        }
        return CompletableFuture.completedFuture(EntityMovement.size())
    }

    private fun writeVoicechat(payload: VoicechatPayload): CompletableFuture<Int?> {
        return this.writeActionAsync(getActionForVoicechatPayload(payload)) { buf ->
            val start = buf.writerIndex()
            payload.record(buf)
            buf.writerIndex() - start
        }
    }

    private fun getActionForVoicechatPayload(payload: VoicechatPayload): FlashbackAction {
        return when (payload.type()) {
            VoicechatPayload.ENCODED_FLASHBACK_TYPE -> FlashbackAction.EncodedVoiceChat
            else -> FlashbackAction.VoiceChat
        }
    }

    public companion object {
        private val IGNORED_PACKETS = setOf(
            ClientboundStartConfigurationPacket::class.java,
            ClientboundFinishConfigurationPacket::class.java,
            ClientboundSetChunkCacheCenterPacket::class.java,
            ClientboundSetSimulationDistancePacket::class.java,
            ClientboundSetChunkCacheRadiusPacket::class.java,
            ClientboundDisconnectPacket::class.java,
            ClientboundCooldownPacket::class.java,
            ClientboundTickingStepPacket::class.java,
            ClientboundTickingStatePacket::class.java,
            ClientboundPlayerPositionPacket::class.java,
            ClientboundMoveMinecartPacket::class.java,

            ClientboundForgetLevelChunkPacket::class.java,
            ClientboundDeleteChatPacket::class.java
        )

        public fun dated(recordings: Path): (ReplayRecorder) -> FlashbackWriter {
            val date = DateTimeUtils.getFormattedDate()
            return { FlashbackWriter(it, FileUtils.findNextAvailable(recordings.resolve(date))) }
        }
    }
}