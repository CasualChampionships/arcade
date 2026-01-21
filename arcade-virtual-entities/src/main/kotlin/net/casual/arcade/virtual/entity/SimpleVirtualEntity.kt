/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.data.PlayerSpecificEntityData
import net.casual.arcade.virtual.entity.utils.VirtualEntityPacketUtils
import net.casual.arcade.virtual.entity.utils.location
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

/**
 * A simple implementation of [VirtualEntity].
 *
 * @param type The type of the virtual entity.
 */
public open class SimpleVirtualEntity(
    public val type: EntityType<*>,
    override val attachment: VirtualEntityAttachment
): TrackingVirtualEntity() {
    private lateinit var lastSyncedPos: Vec3
    private lateinit var lastSyncedRot: Vec2

    /**
     * The entity data for this virtual entity.
     *
     * @see PlayerSpecificEntityData
     */
    protected val data: PlayerSpecificEntityData = PlayerSpecificEntityData(this.type)

    /**
     * Whether player-specific entity data should be
     * persisted after a player stops observing the
     * virtual entity. If data is persisted then
     * if a player starts observing this virtual
     * entity again the previous player-specific
     * data will be sent.
     *
     * Typically, this should be set to `false` to avoid
     * unwanted memory usage.
     */
    public var persistPlayerSpecificData: Boolean = false

    /**
     * Whether this virtual entity is a passenger.
     *
     * If the virtual entity is a passenger then it will
     * not send position update packets.
     */
    public var isPassenger: Boolean = false

    override fun tick() {
        this.sendDirtyEntityData()
        this.sendDirtyLocation()
    }

    override fun sendSpawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        consumer.invoke(this.createSpawnPacket())
        this.sendChangedEntityData(observer, consumer)
    }

    override fun sendDespawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        consumer.invoke(ClientboundRemoveEntitiesPacket(this.id))
    }

    protected open fun createSpawnPacket(): ClientboundAddEntityPacket {
        val location = this.location()
        val (x, y, z) = location.position
        val (xRot, yRot) = location.rotation
        return ClientboundAddEntityPacket(
            this.id, this.uuid, x, y, z, xRot, yRot, this.type, 0, Vec3.ZERO, yRot.toDouble()
        )
    }

    protected open fun sendChangedEntityData(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        val merged = PlayerSpecificEntityData.mergeEntityData(
            this.data.getChangedBaseEntries(),
            this.data.getChangedEntries(observer.uuid)
        )
        if (merged != null) {
            consumer.invoke(ClientboundSetEntityDataPacket(this.id, merged))
        }
    }

    protected open fun sendDirtyLocation() {
        val current = this.location()
        val currentRot = current.rotation
        val previousRot = this.getLastSyncedRotation(currentRot)

        if (this.isPassenger) {
            val packet = VirtualEntityPacketUtils.createRotationPacket(this.id, previousRot, currentRot)
            if (packet != null) {
                this.broadcast(packet)
                this.lastSyncedRot = currentRot
            }
            return
        }

        val currentPos = current.position
        val previousPos = this.getLastSyncedPosition(currentPos)
        val packet = VirtualEntityPacketUtils.createMovePacket(
            this.id, previousPos, currentPos, previousRot, currentRot
        )
        if (packet != null) {
            this.broadcast(packet)
            if (VirtualEntityPacketUtils.isEntityPositionPacket(packet)) {
                this.lastSyncedPos = currentPos
            }
            if (VirtualEntityPacketUtils.isEntityRotationPacket(packet)) {
                this.lastSyncedRot = currentRot
            }
        }
    }

    protected open fun sendDirtyEntityData() {
        val base = this.data.getDirtyBaseEntries()
        for (connection in this.connections) {
            val overridden = this.data.getDirtyEntries(connection.player.uuid)
            val merged = PlayerSpecificEntityData.mergeEntityData(base, overridden)
            if (merged != null) {
                connection.send(ClientboundSetEntityDataPacket(this.id, merged))
            }
        }
    }

    protected fun getLastSyncedPosition(current: Vec3): Vec3 {
        if (!this::lastSyncedPos.isInitialized) {
            this.lastSyncedPos = current
        }
        return this.lastSyncedPos
    }

    protected fun getLastSyncedRotation(current: Vec2): Vec2 {
        if (!this::lastSyncedRot.isInitialized) {
            this.lastSyncedRot = current
        }
        return this.lastSyncedRot
    }
}