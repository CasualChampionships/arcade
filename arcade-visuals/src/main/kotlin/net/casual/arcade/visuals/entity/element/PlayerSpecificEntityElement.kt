/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.element

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData
import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.visuals.entity.data.PlayerSpecificEntityData
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer
import kotlin.experimental.and
import kotlin.experimental.or

public abstract class PlayerSpecificEntityElement: AbstractElement() {
    public val data: PlayerSpecificEntityData = PlayerSpecificEntityData(this.getEntityType())
    public val id: Int = VirtualEntityUtils.requestEntityId()
    public val uuid: UUID = UUID.randomUUID()

    public var sendPositionUpdates: Boolean = true
    public var instantPositionUpdates: Boolean = false
    public var persistPlayerSpecificData: Boolean = false

    private var isRotationDirty = false
    public var xRot: Float = 0.0F
        private set
    public var yRot: Float = 0.0F
        private set

    public fun setXRot(xRot: Float) {
        if (this.xRot != xRot) {
            this.xRot = xRot
            this.isRotationDirty = true
        }
    }

    public fun setYRot(yRot: Float) {
        if (this.yRot != yRot) {
            this.yRot = yRot
            this.isRotationDirty = true
        }
    }

    public fun setPose(pose: Pose) {
        this.data.modifyEntry(EntityTrackedData.POSE) { pose }
    }

    public fun setPoseFor(observer: ServerPlayer, pose: Pose) {
        this.data.set(observer.uuid, EntityTrackedData.POSE, pose)
    }

    public fun setOnFire(onFire: Boolean) {
        this.modifyFlagEntry(EntityTrackedData.ON_FIRE_FLAG_INDEX, onFire)
    }

    public fun setOnFireFor(observer: ServerPlayer, onFire: Boolean) {
        this.modifyFlagEntryFor(observer, EntityTrackedData.ON_FIRE_FLAG_INDEX, onFire)
    }

    public fun setCrouching(crouching: Boolean) {
        this.modifyFlagEntry(EntityTrackedData.SNEAKING_FLAG_INDEX, crouching)
    }

    public fun setCrouchingFor(observer: ServerPlayer, crouching: Boolean) {
        this.modifyFlagEntryFor(observer, EntityTrackedData.SNEAKING_FLAG_INDEX, crouching)
    }

    public fun setSprinting(sprinting: Boolean) {
        this.modifyFlagEntry(EntityTrackedData.SPRINTING_FLAG_INDEX, sprinting)
    }

    public fun setSprintingFor(observer: ServerPlayer, sprinting: Boolean) {
        this.modifyFlagEntryFor(observer, EntityTrackedData.SPRINTING_FLAG_INDEX, sprinting)
    }

    public fun setGlowing(glowing: Boolean) {
        this.modifyFlagEntry(EntityTrackedData.GLOWING_FLAG_INDEX, glowing)
    }

    public fun setGlowingFor(observer: ServerPlayer, glowing: Boolean) {
        this.modifyFlagEntryFor(observer, EntityTrackedData.GLOWING_FLAG_INDEX, glowing)
    }

    public fun setInvisible(invisible: Boolean) {
        this.modifyFlagEntry(EntityTrackedData.INVISIBLE_FLAG_INDEX, invisible)
    }

    public fun setInvisibleFor(observer: ServerPlayer, invisible: Boolean) {
        this.modifyFlagEntryFor(observer, EntityTrackedData.INVISIBLE_FLAG_INDEX, invisible)
    }

    public fun setCustomName(name: Component?) {
        val optional = Optional.ofNullable(name)
        this.data.modifyEntry(EntityTrackedData.CUSTOM_NAME) { optional }
    }

    public fun setCustomNameFor(observer: ServerPlayer, name: Component?) {
        val optional = Optional.ofNullable(name)
        this.data.set(observer.uuid, EntityTrackedData.CUSTOM_NAME, optional)
    }

    public fun setSilent(silent: Boolean) {
        this.data.modifyEntry(EntityTrackedData.SILENT) { silent }
    }

    public fun setSilentFor(observer: ServerPlayer, silent: Boolean) {
        this.data.set(observer.uuid, EntityTrackedData.SILENT, silent)
    }

    public fun setNoGravity(noGravity: Boolean) {
        this.data.modifyEntry(EntityTrackedData.NO_GRAVITY) { noGravity }
    }

    public fun setNoGravityFor(observer: ServerPlayer, noGravity: Boolean) {
        this.data.set(observer.uuid, EntityTrackedData.NO_GRAVITY, noGravity)
    }

    protected fun modifyFlagEntry(flag: Int, value: Boolean) {
        this.data.modifyEntry(EntityTrackedData.FLAGS) { flags ->
            flags.updateFlag(flag, value)
        }
    }

    protected fun modifyFlagEntryFor(observer: ServerPlayer, flag: Int, value: Boolean) {
        val flags = this.data.get(observer.uuid, EntityTrackedData.FLAGS) ?: return
        this.data.set(observer.uuid, EntityTrackedData.FLAGS, flags.updateFlag(flag, value))
    }

    protected open fun createSpawnPacket(observer: ServerPlayer): Packet<ClientGamePacketListener> {
        val pos = this.lastSyncedPos ?: this.currentPos
        return ClientboundAddEntityPacket(
            this.id, this.uuid,
            pos.x, pos.y, pos.z,
            this.xRot, this.yRot,
            this.getEntityType(),
            0, Vec3.ZERO, this.yRot.toDouble()
        )
    }

    protected open fun sendChangedEntityData(
        observer: ServerPlayer,
        sender: Consumer<Packet<ClientGamePacketListener>>
    ) {
        val merged = PlayerSpecificEntityData.mergeEntityData(
            this.data.getChangedBaseEntries(),
            this.data.getChangedEntries(observer.uuid)
        )
        if (merged != null) {
            sender.accept(ClientboundSetEntityDataPacket(this.id, merged))
        }
    }

    protected open fun sendPositionUpdates() {
        val holder = this.holder ?: return
        val pos = this.currentPos
        if (pos == this.lastSyncedPos) {
            return
        }

        val packet = if (this.lastSyncedPos == null) {
            ClientboundEntityPositionSyncPacket(this.id, PositionMoveRotation(pos, Vec3.ZERO, this.yRot, this.xRot), false)
        } else {
            VirtualEntityUtils.createMovePacket(this.id, this.lastSyncedPos, pos, this.isRotationDirty, this.yRot, this.xRot)
        }
        if (packet != null) {
            holder.sendPacket(packet)
            if (packet !is ClientboundMoveEntityPacket.Rot) {
                this.lastSyncedPos = pos
            }
        }
        this.isRotationDirty = false
    }

    protected open fun sendDirtyEntityData() {
        val holder = this.holder ?: return
        val base = this.data.getDirtyBaseEntries()
        for (connection in holder.watchingPlayers) {
            val overridden = this.data.getDirtyEntries(connection.player.uuid)
            val merged = PlayerSpecificEntityData.mergeEntityData(base, overridden)
            if (merged != null) {
                connection.send(ClientboundSetEntityDataPacket(this.id, merged))
            }
        }
    }

    protected open fun sendRotationUpdates() {
        val holder = this.holder ?: return
        if (this.isRotationDirty) {
            val yRot = Mth.floor(this.yRot * 256.0F / 360.0F)
            val xRot = Mth.floor(this.xRot * 256.0F / 360.0F)
            holder.sendPacket(ClientboundMoveEntityPacket.Rot(id, yRot.toByte(), xRot.toByte(), false))
            this.isRotationDirty = false
        }
    }

    override fun getEntityIds(): IntList? {
        return IntList.of(this.id)
    }

    override fun startWatching(
        observer: ServerPlayer,
        sender: Consumer<Packet<ClientGamePacketListener>>
    ) {
        sender.accept(this.createSpawnPacket(observer))
        this.sendChangedEntityData(observer, sender)
    }

    override fun stopWatching(
        observer: ServerPlayer,
        sender: Consumer<Packet<ClientGamePacketListener>>
    ) {
        if (!this.persistPlayerSpecificData) {
            this.data.remove(observer.uuid)
        }
    }

    override fun notifyMove(oldPos: Vec3, newPos: Vec3, delta: Vec3) {
        if (this.sendPositionUpdates && this.instantPositionUpdates) {
            this.sendPositionUpdates()
        }
    }

    override fun tick() {
        this.sendDirtyEntityData()
        if (this.sendPositionUpdates) {
            this.sendPositionUpdates()
        }
        this.sendRotationUpdates()
    }

    protected abstract fun getEntityType(): EntityType<*>

    @Suppress("NOTHING_TO_INLINE")
    protected inline fun Byte.updateFlag(flag: Int, value: Boolean): Byte {
        return if (value) {
            this or (1 shl flag).toByte()
        } else {
            this and (1 shl flag).inv().toByte()
        }
    }
}