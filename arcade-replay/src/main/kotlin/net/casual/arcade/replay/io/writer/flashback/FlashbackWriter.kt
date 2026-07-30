/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.flashback

import com.google.gson.JsonObject
import io.netty.handler.codec.EncoderException
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArraySet
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import kotlinx.atomicfu.atomic
import kotlinx.io.IOException
import net.casual.arcade.replay.compat.voicechat.VoicechatPayload
import net.casual.arcade.replay.io.FlashbackIO
import net.casual.arcade.replay.io.writer.ReplayWriter
import net.casual.arcade.replay.io.writer.ReplayWriter.Companion.close
import net.casual.arcade.replay.mixins.flashback.ClientboundMoveEntityPacketAccessor
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.util.FileUtils
import net.casual.arcade.replay.util.ReplayMarker
import net.casual.arcade.replay.util.flashback.FlashbackAction
import net.casual.arcade.replay.util.flashback.FlashbackMarker.Location
import net.casual.arcade.replay.util.io.ResourcePackCache
import net.casual.arcade.utils.DateTimeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.level.getSpoofedOrRealDimension
import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.ProtocolInfo
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket
import net.minecraft.network.protocol.game.*
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.*
import kotlin.time.Duration

public class FlashbackWriter(
    override val recorder: ReplayRecorder,
    override val path: Path
): ReplayWriter {
    private val executor = ReplayWriter.createExecutor()

    private val writer = FlashbackChunkedWriter(this.path, this.recorder.server.registryAccess(), this.recorder.settings)

    private val positions = Object2ObjectOpenHashMap<ResourceKey<Level>, Int2ObjectOpenHashMap<ExactEntityPosition>>()
    private val dirty = Object2ObjectOpenHashMap<ResourceKey<Level>, IntArraySet>()
    private val chunks = Object2IntOpenHashMap<ChunkPacketIdentity>()
    private val recent = Object2ObjectOpenHashMap<ResourceKey<Level>, Long2IntOpenHashMap>()

    private val forcePlaySnapshot = atomic(false)
    private val resourcePackId = atomic(0)

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
        this.path.createDirectories()
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
            this.forcePlaySnapshot.value = true
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
            is ClientboundAddEntityPacket -> this.initializePosition(packet)
            is ClientboundRemoveEntitiesPacket -> this.removePositions(packet)
            is ClientboundEntityPositionSyncPacket -> return this.updatePosition(packet)
            is ClientboundMoveEntityPacket -> return this.updatePosition(packet)
            is ClientboundResourcePackPushPacket -> this.downloadAndWriteResourcePack(packet)
            is ClientboundCustomPayloadPacket -> when (val payload = packet.payload) {
                is VoicechatPayload -> return this.writeVoicechat(payload)
                else -> packet
            }
            else -> packet
        }

        return this.writeActionAsync(action) { buf ->
            this.writePacketSync(buf, replacement, protocol, offThread)
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
            Vec3.STREAM_CODEC.encode(buf, velocity)
            ByteBufCodecs.GAME_PROFILE.encode(buf, profile)
            buf.writeVarInt(gamemode)
        }
        this.writePosition(player.id, position, rotation, headRot, dirty = false)
        val filtered = packets.filter { it !is ClientboundAddEntityPacket }
        for (packet in filtered) {
            this.recorder.record(packet)
        }
    }

    override fun writeCachedChunk(pos: ChunkPos): Boolean {
        val dimension = this.recorder.level.dimension()
        val chunks = this.recent[dimension] ?: return false
        val posAsLong = pos.pack()
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
        return this.writer.getWrittenBytes()
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

    private fun writePacketSync(
        buf: FriendlyByteBuf,
        packet: Packet<*>,
        protocol: ProtocolInfo<*>,
        offThread: Boolean
    ): Int {
        try {
            val start = buf.writerIndex()
            ReplayWriter.encodePacket(packet, protocol, buf, this.recorder.getPacketContextProvider())
            return buf.writerIndex() - start
        } catch (e: EncoderException) {
            ReplayWriter.handleLoggingEncoderException(LOGGER, packet, protocol, offThread, e)
            throw e
        }
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
                    ReplayWriter.encodePacket(packet, protocol, chunkBuf, this.recorder.getPacketContextProvider())
                    size += (chunkBuf.writerIndex() - start)
                }
                this.chunks.put(identity, index)
            }
            val map = this.recent.getOrPut(dimension, ::Long2IntOpenHashMap)
            map.put(ChunkPos.pack(packet.x, packet.z), index)
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
            LOGGER.error("Something went wrong writing action $action", e)
            null
        }
    }

    private fun writeCustomMeta() {
        try {
            val meta = JsonObject()
            this.recorder.addMetadata(meta)
            val path = this.path.resolve(ReplayWriter.ENTRY_ARCADE_REPLAY_META)
            JsonUtils.encodeRaw(meta, path)
        } catch (exception: Exception) {
            LOGGER.error("Failed to write arcade-replay meta!", exception)
        }
    }

    private fun writeEntityMovement() {
        this.executor.execute {
            val dirty = this.dirty.entries.filter { (_, ids) -> ids.isNotEmpty() }
            if (dirty.isNotEmpty()) {
                this.writer.writeAction(FlashbackAction.MoveEntities) { buf ->
                    buf.writeVarInt(dirty.size)
                    for ((dimension, ids) in dirty) {
                        val positions = this.positions[dimension] ?: continue
                        buf.writeResourceKey(dimension)
                        buf.writeVarInt(ids.size)
                        val iter = ids.iterator()
                        while (iter.hasNext()) {
                            val id = iter.nextInt()
                            buf.writeVarInt(id)
                            positions[id]!!.write(buf)
                        }
                    }
                    this.dirty.clear()
                }
            }
        }
    }

    private fun initializePosition(packet: ClientboundAddEntityPacket): ClientboundAddEntityPacket {
        this.writePosition(
            packet.id,
            Vec3(packet.x, packet.y, packet.z),
            Vec2(packet.xRot, packet.yRot),
            packet.yHeadRot,
            dirty = false
        )
        return packet
    }

    private fun removePositions(packet: ClientboundRemoveEntitiesPacket): ClientboundRemoveEntitiesPacket {
        this.executor.execute {
            val ids = packet.entityIds
            this.dirty[this.currentClientDimension()]?.removeAll(ids)
            this.getPositions().keys.removeAll(ids)
        }
        return packet
    }

    private fun updatePosition(packet: ClientboundEntityPositionSyncPacket): CompletableFuture<Int?> {
        val values = packet.values
        this.writePosition(packet.id, values.position, Vec2(values.xRot, values.yRot), values.yRot)
        return CompletableFuture.completedFuture(ExactEntityPosition.size())
    }

    private fun updatePosition(packet: ClientboundMoveEntityPacket): CompletableFuture<Int?> {
        this.executor.execute {
            val id = (packet as ClientboundMoveEntityPacketAccessor).arcade_getEntityId()
            val positions = this.getPositions()
            val position = positions.get(id) ?: return@execute
            positions.put(id, position.update(packet))
            this.getDirtyPositions().add(id)
        }
        return CompletableFuture.completedFuture(ExactEntityPosition.size())
    }

    private fun writePosition(
        id: Int,
        position: Vec3,
        rotation: Vec2,
        headRot: Float,
        onGround: Boolean = false,
        dirty: Boolean = true
    ) {
        this.executor.execute {
            val position = ExactEntityPosition(position, rotation, headRot, onGround,)
            this.getPositions().put(id, position)
            if (dirty) {
                this.getDirtyPositions().add(id)
            }
        }
    }

    private fun getPositions(): Int2ObjectOpenHashMap<ExactEntityPosition> {
        return this.positions.getOrPut(this.currentClientDimension(), ::Int2ObjectOpenHashMap)
    }

    private fun getDirtyPositions(): IntArraySet {
        return this.dirty.getOrPut(this.currentClientDimension(), ::IntArraySet)
    }

    private fun currentClientDimension(): ResourceKey<Level> {
        return this.recorder.level.getSpoofedOrRealDimension()
    }

    private fun writeVoicechat(payload: VoicechatPayload): CompletableFuture<Int?> {
        return this.writeActionAsync(this.getActionForVoicechatPayload(payload)) { buf ->
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

    private fun downloadAndWriteResourcePack(
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

    private fun writeResourcePack(bytes: ByteArray, expectedHash: String, packId: Int) {
        val realHash = ResourcePackCache.hash(bytes)
        if (expectedHash != "" && expectedHash != realHash) {
            return
        }

        try {
            val directory = this.path.resolve("resource_packs").createDirectories()
            val pack = directory.resolve(realHash)
            if (pack.notExists()) {
                pack.writeBytes(bytes)
            }

            val indexPath = directory.resolve("index.json")
            val index = when {
                indexPath.exists() -> JsonUtils.decodeRaw<HashMap<Int, String>>(indexPath)
                else -> HashMap()
            }
            index[packId] = realHash
            JsonUtils.encodeRaw(index, indexPath)
        } catch (e: IOException) {
            LOGGER.warn("Failed to write resource pack", e)
        }
    }

    public companion object {
        private val LOGGER = LoggerFactory.getLogger("flashback-writer")

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