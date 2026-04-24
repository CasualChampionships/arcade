/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.chunk

import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.replay.events.chunk.ReplayChunkRecorderLoadedResumeEvent
import net.casual.arcade.replay.events.chunk.ReplayChunkRecorderSnapshotEvent
import net.casual.arcade.replay.events.chunk.ReplayChunkRecorderUnloadedPauseEvent
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.mixins.chunk.WitherBossAccessor
import net.casual.arcade.replay.mixins.rejoin.ChunkMapAccessor
import net.casual.arcade.replay.recorder.ChunkSender
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.replay.recorder.rejoin.RejoinConnection
import net.casual.arcade.replay.recorder.rejoin.RejoinedReplayPlayer
import net.casual.arcade.replay.recorder.settings.RecorderSettings
import net.casual.arcade.replay.recorder.settings.RecorderSettings.ChunkRecordingStrategy
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ClientboundAddEntityPacket
import net.casual.arcade.utils.compat.PolymerCompatLayer
import net.casual.arcade.utils.entity.WrappedTrackedEntity
import net.casual.arcade.utils.level.server
import net.casual.arcade.utils.toIdString
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl
import net.minecraft.core.UUIDUtil
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.server.TickTask
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.builder.ToStringBuilder
import org.jetbrains.annotations.ApiStatus.Internal
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * An implementation of [ReplayRecorder] for recording chunk areas.
 *
 * @param chunks The [ChunkArea] to record.
 * @param recorderName The name of the [ReplayChunkRecorder].
 * @see ReplayPlayerRecorder
 * @see ReplayChunkRecorder
 */
public class ReplayChunkRecorder internal constructor(
    public val chunks: ChunkArea,
    public val recorderName: String,
    settings: RecorderSettings,
    format: ReplayFormat,
    path: Path,
): ReplayRecorder(chunks.level.server(), PROFILE, settings, format, path), ChunkSender {
    private val dummy by lazy {
        val player = ServerPlayer(this.server, this.chunks.level, PROFILE, ClientInformation.createDefault())
        ReplayChunkGamePacketListener(this, player, this.connection)
        player
    }

    private val connection = this.createConnection()

    private val trackedPlayers = ReferenceOpenHashSet<ServerPlayer>()

    private val loadedChunks = LongOpenHashSet()
    private val sentChunks = LongOpenHashSet()

    private val recordables = HashSet<ReplayChunkRecordable>()

    private var dummySpawnPos : Vec3? = null

    /**
     * The level that the chunk recording is currently in.
     */
    override val level: ServerLevel
        get() = this.chunks.level

    /**
     * The current position of the recorder.
     */
    override val position: Vec3
        get() = this.dummy.position()

    /**
     * The current rotation of the recorder.
     */
    override val rotation: Vec2
        get() = Vec2.ZERO

    public fun setDummySpawnPos(pos: Vec3) {
        dummySpawnPos = pos;
    }

    override fun record(outgoing: Packet<*>) {
        super.record(PolymerCompatLayer.replacePacket(this.dummy.connection, outgoing))
    }

    /**
     * This gets the name of the replay recording.
     *
     * @return The name of the replay recording.
     */
    override fun getName(): String {
        return this.recorderName
    }

    /**
     * This starts the replay recording, it sends all the chunk and
     * entity packets as if a player were logging into the server.
     *
     * This method should just simulate
     */
    override fun initialize(): Boolean {
        val center = this.getCenterChunk()
        // Load the chunk
        this.level.getChunk(center.x, center.z)

        if(dummySpawnPos != null){
            this.dummy.setPosRaw(dummySpawnPos!!.x, dummySpawnPos!!.y, dummySpawnPos!!.z)
        } else {
            val x = center.middleBlockX
            val z = center.middleBlockZ
            val y = this.level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z)
            this.dummy.setPosRaw(x.toDouble(), y + 10.0, z.toDouble())
        }
        this.dummy.setServerLevel(this.level)
        this.dummy.isInvisible = true

        RejoinedReplayPlayer.rejoin(this.dummy, this)
        this.spawnPlayer()
        this.sendChunksAndEntities()
        GlobalEventHandler.Server.broadcast(ReplayChunkRecorderSnapshotEvent(this, true))

        val chunks = this.level.chunkSource.chunkMap as ChunkMapAccessor
        for (pos in this.chunks) {
            val holder = chunks.getTickingChunk(pos.pack())
            if (holder != null) {
                (holder as ReplayChunkRecordable).addRecorder(this)
            }
        }

        return true
    }

    /**
     * This method tries to restart the replay recorder by creating
     * a new instance of itself.
     *
     * @return Whether it successfully restarted.
     */
    override fun restart(): Boolean {
        val recorder = ReplayChunkRecorders.create(this.chunks, this.path, this.format, this.settings, this.recorderName)
        return recorder.start(StartingMode.Restart)
    }

    /**
     * Whether the current recorder can pause recording.
     *
     * @return Whether it can be paused.
     */
    override fun canPauseRecording(): Boolean {
        return this.format == ReplayFormat.Flashback
    }

    /**
     * This gets called when the replay is closing. It removes all [ReplayChunkRecordable]s
     * and updates the [ReplayChunkRecorders] manager.
     *
     * @param future The future that will complete once the replay has closed.
     */
    override fun onClosing(future: CompletableFuture<Long>) {
        for (recordable in ArrayList(this.recordables)) {
            recordable.removeRecorder(this)
        }

        if (this.recordables.isNotEmpty()) {
            ArcadeUtils.logger.warn("Failed to unlink all chunk recordables")
        }

        ReplayChunkRecorders.close(this.server, this, future)
    }

    /**
     * Returns whether a given player should be hidden from the player tab list.
     *
     * @return Whether the player should be hidden
     */
    override fun shouldHidePlayerFromTabList(player: ServerPlayer): Boolean {
        return this.dummy == player
    }

    /**
     * This appends any additional data to the status.
     *
     * @param builder The [ToStringBuilder] which is used to build the status.
     * @see getStatus
     */
    override fun appendToStatus(builder: ToStringBuilder) {
        builder.append("chunks_world", this.chunks.level.dimension().identifier())
        builder.append("chunks_from", this.chunks.from)
        builder.append("chunks_to", this.chunks.to)
    }

    /**
     * This allows you to add any additional metadata which will be
     * saved in the replay file.
     *
     * @param meta The JSON metadata map which can be mutated.
     */
    override fun addMetadata(meta: JsonObject) {
        super.addMetadata(meta)
        meta.addProperty("chunks_world", this.chunks.level.dimension().toIdString())
        meta.addProperty("chunks_from", this.chunks.from.toString())
        meta.addProperty("chunks_to", this.chunks.to.toString())
    }

    /**
     * This gets the center chunk position of the chunk recording.
     *
     * @return The center most chunk position.
     */
    override fun getCenterChunk(): ChunkPos {
        return this.chunks.center
    }

    /**
     * This will iterate over every chunk position that is going
     * to be sent, each chunk position will be accepted into the
     * [consumer].
     *
     * @param consumer The consumer that will accept the given chunks positions.
     */
    override fun forEachChunk(consumer: Consumer<ChunkPos>) {
        val radius = this.settings.chunkRecorderLoadRadius
        if (radius < 0) {
            this.chunks.forEach(consumer)
            return
        }

        val copy = this.sentChunks.longStream().mapToObj { ChunkPos.unpack(it) }
            .collect(Collectors.toCollection(::ArrayList))
        ChunkPos.rangeClosed(this.chunks.center, radius + 1).filter {
            this.chunks.contains(this.level.dimension(), it)
        }.collect(Collectors.toCollection { copy })
        copy.forEach(consumer)
    }

    /**
     * This determines whether a given [entity] should be tracked.
     *
     * @param entity The entity to check.
     * @param range The entity's tracking range.
     * @return Whether the entity should be tracked.
     */
    override fun shouldTrackEntity(entity: Entity, range: Double): Boolean {
        return this.chunks.contains(entity.level().dimension(), entity.chunkPosition())
    }

    /**
     * This records a packet.
     *
     * @param packet The packet to be recorded.
     */
    override fun sendChunkPacket(packet: Packet<*>) {
        this.record(packet)
    }

    /**
     * This is called when [shouldTrackEntity] returns `true`,
     * this should be used to send any additional packets for this entity.
     *
     * @param tracked The [WrappedTrackedEntity].
     */
    override fun addTrackedEntity(tracked: WrappedTrackedEntity) {
        (tracked.tracked as ReplayChunkRecordable).addRecorder(this)
    }

    /**
     * This gets the view distance of the chunk area.
     *
     * @return The view distance of the chunk area.
     */
    override fun getViewDistance(): Int {
        return this.chunks.viewDistance
    }

    override fun onChunkSent(chunk: LevelChunk) {
        this.sentChunks.add(chunk.pos.pack())
    }

    /**
     * Determines whether a given packet is able to be recorded.
     *
     * @param packet The packet that is going to be recorded.
     * @return Whether this recorded should record it.
     */
    override fun canRecordPacket(packet: Packet<*>): Boolean {
        // If the server view-distance changes we do not want to update
        // the client - this will cut the view distance in the replay
        if (packet is ClientboundSetChunkCacheRadiusPacket) {
            return packet.radius == this.getViewDistance()
        }
        return super.canRecordPacket(packet)
    }

    override fun takeSnapshot() {
        RejoinedReplayPlayer.rejoin(this.dummy, this)
        this.spawnPlayer()
        this.sendChunkViewDistance()
        this.sendItemFrameMapData { frame -> this.chunks.contains(frame.chunkPosition()) }
        this.sendChunks(ChunkSender.SeenEntities.all()) { pos -> this.writer.writeCachedChunk(pos) }
        for (recordable in this.recordables) {
            recordable.resendPackets(this)
        }
        GlobalEventHandler.Server.broadcast(ReplayChunkRecorderSnapshotEvent(this, true))
    }

    override fun getPacketContextProvider(): Connection {
        return this.connection
    }

    /**
     * This gets the dummy chunk recording player.
     *
     * **This is *not* a real player, and many operations on this instance
     * may cause crashes, be very careful with how you use this.**
     *
     * @return The dummy chunk recording player.
     */
    public fun getDummyPlayer(): ServerPlayer {
        return this.dummy
    }

    @Internal
    override fun tick() {
        super.tick()

        if (this.settings.chunkRecordingStrategy == ChunkRecordingStrategy.ChunkContainsNonSpectatorPlayer) {
            if (this.trackedPlayers.all(ServerPlayer::isSpectator)) {
                this.tryPauseAndBroadcast()
            } else {
                this.tryResumeAndBroadcast()
            }
        }
    }

    @Internal
    public fun addRecordable(recordable: ReplayChunkRecordable) {
        this.recordables.add(recordable)
    }

    @Internal
    public fun removeRecordable(recordable: ReplayChunkRecordable) {
        this.recordables.remove(recordable)
    }

    @Internal
    public fun onEntityTracked(entity: Entity) {
        if (entity is WitherBoss) {
            val recordable = ((entity as WitherBossAccessor).bossEvent as ReplayChunkRecordable)
            recordable.addRecorder(this)
        } else if (entity is ServerPlayer) {
            this.trackedPlayers.add(entity)

            val resume = when (this.settings.chunkRecordingStrategy) {
                ChunkRecordingStrategy.ChunkContainsPlayer -> true
                ChunkRecordingStrategy.ChunkContainsNonSpectatorPlayer -> !entity.isSpectator
                else -> false
            }

            if (resume) {
                this.tryResumeAndBroadcast()
            }
        }
    }

    @Internal
    public fun onEntityUntracked(entity: Entity) {
        if (entity is WitherBoss) {
            val recordable = ((entity as WitherBossAccessor).bossEvent as ReplayChunkRecordable)
            recordable.removeRecorder(this)
        } else if (entity is ServerPlayer) {
            this.trackedPlayers.remove(entity)

            val pause = when (this.settings.chunkRecordingStrategy) {
                ChunkRecordingStrategy.ChunkContainsPlayer -> this.trackedPlayers.isEmpty()
                ChunkRecordingStrategy.ChunkContainsNonSpectatorPlayer -> this.trackedPlayers.all { it.isSpectator }
                else -> false
            }

            if (pause) {
                this.tryPauseAndBroadcast()
            }
        }
    }

    @Internal
    public fun onChunkLoaded(chunk: LevelChunk) {
        if (!this.chunks.contains(chunk.level.dimension(), chunk.pos)) {
            ArcadeUtils.logger.error("Tried to load chunk out of bounds!")
            return
        }

        this.loadedChunks.add(chunk.pos.pack())
        if (this.settings.chunkRecordingStrategy == ChunkRecordingStrategy.ChunkLoaded) {
            // We need to schedule this because otherwise we could run into a CME
            // as this method is called while iterating chunks, and we may cause
            // chunks to be loaded when taking a flashback snapshot
            val task = TickTask(this.server.tickCount, this::tryResumeAndBroadcast)
            this.server.schedule(task)
        }

        if (!this.sentChunks.contains(chunk.pos.pack())) {
            this.sendChunk(chunk, ChunkSender.SeenEntities.mutable())
        }
    }

    @Internal
    public fun onChunkUnloaded(pos: ChunkPos, chunk: LevelChunk?) {
        if (!this.chunks.contains(this.level.dimension(), pos)) {
            ArcadeUtils.logger.error("Tried to unload chunk out of bounds!")
            return
        }

        if (chunk != null && this.writer.cacheChunksOnUnload) {
            val packet = ClientboundLevelChunkWithLightPacket(
                chunk, this.level.lightEngine, null, null
            )
            this.record(packet)
        }

        this.loadedChunks.remove(pos.pack())

        if (this.loadedChunks.isEmpty()) {
            if (this.settings.chunkRecordingStrategy == ChunkRecordingStrategy.ChunkLoaded) {
                this.tryPauseAndBroadcast(true)
            }
        }
    }

    private fun tryPauseAndBroadcast(force: Boolean = false) {
        if (this.pause(force)) {
            GlobalEventHandler.Server.broadcast(ReplayChunkRecorderUnloadedPauseEvent(this))
        }
    }

    private fun tryResumeAndBroadcast() {
        if (this.resume()) {
            GlobalEventHandler.Server.broadcast(ReplayChunkRecorderLoadedResumeEvent(this))
        }
    }

    private fun spawnPlayer() {
        val spawnPackets = ArrayList<Packet<*>>(3)
        spawnPackets.add(ClientboundAddEntityPacket(this.dummy))
        val tracked = this.dummy.entityData.nonDefaultValues
        if (tracked != null) {
            spawnPackets.add(ClientboundSetEntityDataPacket(this.dummy.id, tracked))
            spawnPackets.add(ClientboundUpdateMobEffectPacket(this.dummy.id, INVISIBILITY, false))
        }
        this.spawnPlayer(this.dummy, spawnPackets)
    }

    @Suppress("UnstableApiUsage")
    private fun createConnection(): Connection {
        val connection = RejoinConnection()
        connection.packetContext.set(PacketContextImpl.GAME_PROFILE, PROFILE)
        connection.packetContext.set(PacketContextImpl.SERVER_INSTANCE, this.server)
        connection.packetContext.set(PacketContextImpl.REGISTRY_ACCESS, this.server.registryAccess())
        return connection
    }

    private companion object {
        private val PROFILE = UUIDUtil.createOfflineProfile("-ChunkRecorder-")

        private val INVISIBILITY = MobEffectInstance(MobEffects.INVISIBILITY, MobEffectInstance.INFINITE_DURATION, 0, false, true)
    }
}