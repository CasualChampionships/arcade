/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.player

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.replay.compat.polymer.PolymerPacketPatcher
import net.casual.arcade.replay.events.player.ReplayPlayerRecorderSnapshotEvent
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.ChunkSender
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.recorder.rejoin.RejoinedReplayPlayer
import net.casual.arcade.utils.impl.WrappedTrackedEntity
import net.casual.arcade.replay.io.writer.ReplayWriter
import net.casual.arcade.replay.recorder.settings.RecorderSettings
import net.casual.arcade.utils.ClientboundAddEntityPacket
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ChunkTrackingView
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.jetbrains.annotations.ApiStatus.Internal
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.io.path.nameWithoutExtension

/**
 * An implementation of [ReplayRecorder] for recording players.
 *
 * @param server The [MinecraftServer] instance.
 * @param profile The profile of the player being recorded.
 * @see ReplayRecorder
 */
public class ReplayPlayerRecorder internal constructor(
    server: MinecraftServer,
    profile: GameProfile,
    settings: RecorderSettings,
    format: ReplayFormat,
    path: Path,
): ReplayRecorder(server, profile, settings, format, path), ChunkSender {
    private val player: ServerPlayer?
        get() = this.server.playerList.getPlayer(this.recordingPlayerUUID)

    /**
     * The level that the player is currently in.
     */
    override val level: ServerLevel
        get() = this.player?.level() ?: this.server.overworld()

    /**
     * The current position of the player.
     */
    override val position: Vec3
        get() = this.getPlayerOrThrow().position()

    /**
     * The current rotation of the player.
     */
    override val rotation: Vec2
        get() = this.getPlayerOrThrow().rotationVector

    /**
     * Gets the player that's being recorded.
     * If the player doesn't exist, an exception will be thrown.
     *
     * The exception will only be thrown *if* this method is called
     * in the case a [ReplayPlayerRecorder] was started as a result of the
     * player logging in and the player has not finished logging in yet.
     *
     * @return The player that is being recorded.
     */
    public fun getPlayerOrThrow(): ServerPlayer {
        return this.player ?: throw IllegalStateException("Tried to get player before player joined")
    }

    /**
     * This gets the name of the replay recording.
     * In the case for [ReplayPlayerRecorder]s it's just the name of
     * the player.
     *
     * @return The name of the replay recording.
     */
    override fun getName(): String {
        return this.profile.name
    }

    /**
     * This starts the replay recording, note this is **not** called
     * to start a replay if a player is being recorded from the login phase.
     *
     * This method should just simulate the player joining.
     *
     * @return Whether initialization was successful.
     */
    override fun initialize(): Boolean {
        val player = this.player ?: return false
        RejoinedReplayPlayer.rejoin(player, this)
        this.spawnPlayer(player, listOf(ClientboundAddEntityPacket(player)))
        this.sendChunksAndEntities()
        GlobalEventHandler.Server.broadcast(ReplayPlayerRecorderSnapshotEvent(this, true))
        return true
    }

    /**
     * This method tries to restart the replay recorder by creating
     * a new instance of itself.
     *
     * @return Whether it successfully restarted.
     */
    override fun restart(): Boolean {
        if (this.player == null) {
            return false
        }
        val recorder = ReplayPlayerRecorders.create(this.server, this.profile, this.path, this.format, this.settings)
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
     * This updates the [ReplayPlayerRecorders] manager.
     *
     * @param future The future that will complete once the replay has closed.
     */
    override fun onClosing(future: CompletableFuture<Long>) {
        ReplayPlayerRecorders.close(this.server, this, future)
    }

    override fun takeSnapshot() {
        RejoinedReplayPlayer.rejoin(this.getPlayerOrThrow(), this)
        this.sendChunksAndEntities { pos -> this.writer.writeCachedChunk(pos) }
        GlobalEventHandler.Server.broadcast(ReplayPlayerRecorderSnapshotEvent(this, false))
    }

    /**
     * The player's chunk position.
     *
     * @return The player's chunk position.
     */
    override fun getCenterChunk(): ChunkPos {
        return this.getPlayerOrThrow().chunkPosition()
    }

    /**
     * This method iterates over all the chunk positions in the player's
     * view distance accepting a [consumer].
     *
     * @param consumer The consumer that will accept the given chunks positions.
     */
    override fun forEachChunk(consumer: Consumer<ChunkPos>) {
        ChunkTrackingView.of(this.getCenterChunk(), this.server.playerList.viewDistance).forEach(consumer)
    }

    /**
     * This records a packet.
     *
     * @param packet The packet to be recorded.
     */
    override fun sendChunkPacket(packet: Packet<*>) {
        this.record(PolymerPacketPatcher.replace(this.getPlayerOrThrow().connection, packet))
    }

    /**
     * This determines whether a given [entity] should be sent.
     * Whether the entity is within the player's tracking range.
     *
     * @param entity The entity to check.
     * @param range The entity's tracking range.
     * @return Whether the entity should be tracked.
     */
    override fun shouldTrackEntity(entity: Entity, range: Double): Boolean {
        val player = this.getPlayerOrThrow()
        val delta = player.position().subtract(entity.position())
        val deltaSqr = delta.x * delta.x + delta.z * delta.z
        val rangeSqr = range * range
        return deltaSqr <= rangeSqr && entity.broadcastToPlayer(player)
    }

    /**
     * This pairs the data of the tracked entity with the replay recorder.
     *
     * @param tracked The tracked entity.
     */
    override fun addTrackedEntity(tracked: WrappedTrackedEntity) {
        val list = ArrayList<Packet<ClientGamePacketListener>>()
        tracked.getServerEntity().sendPairingData(this.getPlayerOrThrow(), list::add)
        this.sendChunkPacket(ClientboundBundlePacket(list))
    }

    /**
     * This records the recording player.
     *
     * @param entity The recording player's [ServerEntity].
     */
    @Internal
    public fun spawnPlayer(entity: ServerEntity) {
        val list = ArrayList<Packet<ClientGamePacketListener>>()
        val player = this.getPlayerOrThrow()
        entity.sendPairingData(player, list::add)
        this.spawnPlayer(player, list)
    }

    /**
     * This removes the recording player.
     *
     * @param player The recording player.
     */
    @Internal
    public fun removePlayer(player: ServerPlayer) {
        this.record(ClientboundRemoveEntitiesPacket(player.id))
    }
}