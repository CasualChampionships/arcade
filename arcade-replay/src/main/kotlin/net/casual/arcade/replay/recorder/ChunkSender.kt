/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSet
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.getTrackedEntities
import net.casual.arcade.utils.impl.WrappedTrackedEntity
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.LevelChunk
import org.jetbrains.annotations.ApiStatus.*
import java.util.function.Consumer
import kotlin.math.min

/**
 * This interface provides a way to resend any given chunks
 * and any entities within those chunks.
 *
 * @see ReplayPlayerRecorder
 * @see ReplayChunkRecorder
 */
public interface ChunkSender {
    /**
     * The level of which the chunks are in.
     */
    public val level: ServerLevel

    /**
     * The center chunk position of all the chunks being sent.
     *
     * @return The center most chunk position.
     */
    public fun getCenterChunk(): ChunkPos

    /**
     * This will iterate over every chunk position that is going
     * to be sent, each chunk position will be accepted into the
     * [consumer].
     *
     * @param consumer The consumer that will accept the given chunks positions.
     */
    public fun forEachChunk(consumer: Consumer<ChunkPos>)

    /**
     * This determines whether a given [entity] should be sent.
     *
     * @param entity The entity to check.
     * @param range The entity's tracking range.
     * @return Whether the entity should be tracked.
     */
    public fun shouldTrackEntity(entity: Entity, range: Double): Boolean

    /**
     * This method should consume a packet.
     * This is used to send the chunk and entity packets.
     *
     * @param packet The packet to send.
     */
    public fun sendChunkPacket(packet: Packet<*>)

    /**
     * This is called when [shouldTrackEntity] returns `true`,
     * this should be used to send any additional packets for this entity.
     *
     * @param tracked The [WrappedTrackedEntity].
     */
    public fun addTrackedEntity(tracked: WrappedTrackedEntity)

    /**
     * This gets the view distance of the server.
     *
     * @return The view distance of the server.
     */
    public fun getViewDistance(): Int {
        return this.level.server.playerList.viewDistance
    }

    /**
     * This is called when a chunk is successfully sent to the client.
     *
     * @param chunk The chunk that was sent.
     */
    @OverrideOnly
    public fun onChunkSent(chunk: LevelChunk) {

    }

    /**
     * This sends all chunk and entity packets.
     */
    @NonExtendable
    public fun sendChunksAndEntities(unloaded: (ChunkPos) -> Boolean = { false }) {
        val seen = SeenEntities.mutable()
        this.sendChunkViewDistance()
        this.sendChunks(seen, unloaded)
        this.sendChunkEntities(seen)
    }

    /**
     * This sends all the chunk view distance and simulation distance packets.
     */
    @Internal
    public fun sendChunkViewDistance() {
        val center = this.getCenterChunk()
        this.sendChunkPacket(ClientboundSetChunkCacheCenterPacket(center.x, center.z))
        this.sendChunkPacket(ClientboundSetChunkCacheRadiusPacket(this.getViewDistance()))
        this.sendChunkPacket(ClientboundSetSimulationDistancePacket(this.getViewDistance()))
    }

    /**
     * This sends all chunk packets.
     *
     * @param seen The [IntSet] of entity ids that have already been seen.
     */
    @Internal
    public fun sendChunks(seen: SeenEntities, unloaded: (ChunkPos) -> Boolean = { false }) {
        val source = this.level.chunkSource
        this.forEachChunk { pos ->
            var chunk = source.getChunk(pos.x, pos.z, false)
            if (chunk != null) {
                this.sendChunk(chunk, seen)
            } else if (!unloaded.invoke(pos)) {
                chunk = source.getChunk(pos.x, pos.z, true)
                if (chunk != null) {
                    this.sendChunk(chunk, seen)
                } else {
                    ArcadeUtils.logger.warn("Failed to get chunk at $pos, didn't send")
                }
            }
        }
    }

    /**
     * This sends a specific chunk packet.
     *
     * @param chunk The current chunk that is being sent.
     * @param seen The [IntSet] of entity ids that have already been seen.
     */
    @Internal
    public fun sendChunk(
        chunk: LevelChunk,
        seen: SeenEntities,
    ) {
        // We don't need to use the chunkSender
        // We are only writing the packets to disk...
        this.sendChunkPacket(ClientboundLevelChunkWithLightPacket(
            chunk,
            chunk.level.lightEngine,
            null,
            null
        ))

        val leashed = ArrayList<Mob>()
        val ridden = ArrayList<Entity>()

        val viewDistance = this.level.server.playerList.viewDistance
        for (tracked in this.level.getTrackedEntities()) {
            val entity = tracked.getEntity()
            tracked.getServerEntity()
            if (entity.chunkPosition() == chunk.pos) {
                if (!seen.has(entity.id)) {
                    val range = min(tracked.getRange(), viewDistance * 16).toDouble()
                    if (this.shouldTrackEntity(entity, range)) {
                        this.addTrackedEntity(tracked)
                        seen.add(entity.id)
                    }
                }

                if (entity is Mob && entity.leashHolder != null) {
                    leashed.add(entity)
                }
                if (entity.passengers.isNotEmpty()) {
                    ridden.add(entity)
                }
            }
        }

        for (entity in leashed) {
            this.sendChunkPacket(ClientboundSetEntityLinkPacket(entity, entity.leashHolder))
        }
        for (entity in ridden) {
            this.sendChunkPacket(ClientboundSetPassengersPacket(entity))
        }

        this.onChunkSent(chunk)
    }

    /**
     * This sends all the entities.
     *
     * @param seen The [IntSet] of entity ids that have already been seen.
     */
    @Internal
    public fun sendChunkEntities(seen: SeenEntities) {
        val viewDistance = this.level.server.playerList.viewDistance
        for (tracked in this.level.getTrackedEntities()) {
            val entity = tracked.getEntity()
            val range = min(tracked.getRange(), viewDistance * 16).toDouble()
            if (this.shouldTrackEntity(entity, range)) {
                this.addTrackedEntity(tracked)
            }
        }
    }

    public interface SeenEntities {
        public fun has(id: Int): Boolean

        public fun add(id: Int): Boolean

        private object All: SeenEntities {
            override fun has(id: Int): Boolean {
                return true
            }

            override fun add(id: Int): Boolean {
                return false
            }
        }

        private class Mutable: SeenEntities {
            val seen = IntOpenHashSet()

            override fun has(id: Int): Boolean {
                return this.seen.contains(id)
            }

            override fun add(id: Int): Boolean {
                return this.seen.add(id)
            }
        }

        public companion object {
            public fun all(): SeenEntities {
                return All
            }

            public fun mutable(): SeenEntities {
                return Mutable()
            }
        }
    }
}