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
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors
import net.casual.arcade.virtual.entity.utils.EntityDataSharedFlags
import net.casual.arcade.virtual.entity.utils.VirtualEntityPacketUtils
import net.casual.arcade.virtual.entity.utils.location
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * A simple implementation of [VirtualEntity].
 *
 * This virtual entity can be any [type], appropriate
 * default entity data will be generated.
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

    override fun observableRange(): Double {
        return this.type.clientTrackingRange() * 16.0
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

    public fun <T: Any> setDataEntry(accessor: EntityDataAccessor<T>, value: T) {
        this.modifyDataEntry(accessor) { value }
    }
    
    public fun <T: Any> setDataEntryFor(observer: ServerPlayer, accessor: EntityDataAccessor<T>, value: T) {
        this.data.set(observer.uuid, accessor, value)
    }
    
    public fun <T: Any> setBaseDataEntryFor(observer: ServerPlayer, accessor: EntityDataAccessor<T>) {
        this.data.setToBase(observer.uuid, accessor)
    }
    
    public fun <T: Any> modifyDataEntry(accessor: EntityDataAccessor<T>, modifier: (T) -> T) {
        this.data.modify(accessor, false, modifier)
    }

    public fun setPose(pose: Pose) {
        this.setDataEntry(EntityDataAccessors.POSE, pose)
    }

    public fun setPoseFor(observer: ServerPlayer, pose: Pose) {
        this.setDataEntryFor(observer, EntityDataAccessors.POSE, pose)
    }

    public fun setBasePoseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, EntityDataAccessors.POSE)
    }

    public fun modifyPose(modifier: (Pose) -> Pose) {
        this.modifyDataEntry(EntityDataAccessors.POSE) { current -> modifier.invoke(current) }
    }

    public fun setOnFire(onFire: Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.ON_FIRE, onFire)
    }

    public fun setOnFireFor(observer: ServerPlayer, onFire: Boolean) {
        this.modifyFlagEntryFor(observer, EntityDataSharedFlags.ON_FIRE, onFire)
    }

    public fun setOnFireToBaseFor(observer: ServerPlayer) {
        this.modifyFlagEntryToBaseFor(observer, EntityDataSharedFlags.ON_FIRE)
    }

    public fun modifyOnFire(modifier: (Boolean) -> Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.ON_FIRE, modifier)
    }

    public fun setCrouching(crouching: Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.SHIFT_KEY_DOWN, crouching)
    }

    public fun setCrouchingFor(observer: ServerPlayer, crouching: Boolean) {
        this.modifyFlagEntryFor(observer, EntityDataSharedFlags.SHIFT_KEY_DOWN, crouching)
    }

    public fun setCrouchingToBaseFor(observer: ServerPlayer) {
        this.modifyFlagEntryToBaseFor(observer, EntityDataSharedFlags.SHIFT_KEY_DOWN)
    }

    public fun modifyCrouching(modifier: (Boolean) -> Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.SHIFT_KEY_DOWN, modifier)
    }

    public fun setSprinting(sprinting: Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.SPRINTING, sprinting)
    }

    public fun setSprintingFor(observer: ServerPlayer, sprinting: Boolean) {
        this.modifyFlagEntryFor(observer, EntityDataSharedFlags.SPRINTING, sprinting)
    }

    public fun setSprintingToBaseFor(observer: ServerPlayer) {
        this.modifyFlagEntryToBaseFor(observer, EntityDataSharedFlags.SPRINTING)
    }

    public fun modifySprinting(modifier: (Boolean) -> Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.SPRINTING, modifier)
    }

    public fun setGlowing(glowing: Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.GLOWING, glowing)
    }

    public fun setGlowingFor(observer: ServerPlayer, glowing: Boolean) {
        this.modifyFlagEntryFor(observer, EntityDataSharedFlags.GLOWING, glowing)
    }

    public fun setGlowingToBaseFor(observer: ServerPlayer) {
        this.modifyFlagEntryToBaseFor(observer, EntityDataSharedFlags.GLOWING)
    }

    public fun modifyGlowing(modifier: (Boolean) -> Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.GLOWING, modifier)
    }

    public fun setInvisible(invisible: Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.INVISIBLE, invisible)
    }

    public fun setInvisibleFor(observer: ServerPlayer, invisible: Boolean) {
        this.modifyFlagEntryFor(observer, EntityDataSharedFlags.INVISIBLE, invisible)
    }

    public fun setInvisibleToBaseFor(observer: ServerPlayer) {
        this.modifyFlagEntryToBaseFor(observer, EntityDataSharedFlags.INVISIBLE)
    }

    public fun modifyInvisible(modifier: (Boolean) -> Boolean) {
        this.modifyFlagEntry(EntityDataSharedFlags.INVISIBLE, modifier)
    }

    public fun setCustomName(name: Component?) {
        val optional = Optional.ofNullable(name)
        this.setDataEntry(EntityDataAccessors.CUSTOM_NAME, optional)
    }

    public fun setCustomNameFor(observer: ServerPlayer, name: Component?) {
        val optional = Optional.ofNullable(name)
        this.setDataEntryFor(observer, EntityDataAccessors.CUSTOM_NAME, optional)
    }

    public fun setCustomNameToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, EntityDataAccessors.CUSTOM_NAME)
    }

    public fun modifyCustomName(modifier: (Component?) -> Component?) {
        this.modifyDataEntry(EntityDataAccessors.CUSTOM_NAME) { current ->
            Optional.ofNullable(modifier.invoke(current.getOrNull()))
        }
    }

    public fun setSilent(silent: Boolean) {
        this.setDataEntry(EntityDataAccessors.SILENT, silent)
    }

    public fun setSilentFor(observer: ServerPlayer, silent: Boolean) {
        this.setDataEntryFor(observer, EntityDataAccessors.SILENT, silent)
    }

    public fun setSilentToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, EntityDataAccessors.SILENT)
    }

    public fun modifySilent(modifier: (Boolean) -> Boolean) {
        this.modifyDataEntry(EntityDataAccessors.SILENT) { current -> modifier.invoke(current) }
    }

    public fun setNoGravity(noGravity: Boolean) {
        this.setDataEntry(EntityDataAccessors.NO_GRAVITY, noGravity)
    }

    public fun setNoGravityFor(observer: ServerPlayer, noGravity: Boolean) {
        this.setDataEntryFor(observer, EntityDataAccessors.NO_GRAVITY, noGravity)
    }

    public fun setNoGravityToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, EntityDataAccessors.NO_GRAVITY)
    }

    public fun modifyNoGravity(modifier: (Boolean) -> Boolean) {
        this.modifyDataEntry(EntityDataAccessors.NO_GRAVITY) { current -> modifier.invoke(current) }
    }

    protected fun modifyFlagEntry(flag: Int, value: Boolean) {
        this.modifyDataEntry(EntityDataAccessors.SHARED_FLAGS) { flags ->
            EntityDataSharedFlags.updateFlag(flags, flag, value)
        }
    }

    protected fun modifyFlagEntryFor(observer: ServerPlayer, flag: Int, value: Boolean) {
        val flags = this.data.get(observer.uuid, EntityDataAccessors.SHARED_FLAGS) ?: return
        this.setDataEntryFor(
            observer, EntityDataAccessors.SHARED_FLAGS, EntityDataSharedFlags.updateFlag(flags, flag, value)
        )
    }

    protected fun modifyFlagEntryToBaseFor(observer: ServerPlayer, flag: Int) {
        val base = this.data.getBaseEntry(EntityDataAccessors.SHARED_FLAGS) ?: return
        if (this.data.isOverridden(observer.uuid, EntityDataAccessors.SHARED_FLAGS)) {
            val current = (base.value.toInt() shr flag) and 1 != 0
            this.modifyFlagEntryFor(observer, flag, current)
        }
    }

    protected fun modifyFlagEntry(flag: Int, modifier: (Boolean) -> Boolean) {
        this.modifyDataEntry(EntityDataAccessors.SHARED_FLAGS) { flags ->
            val current = (flags.toInt() shr flag) and 1 != 0
            EntityDataSharedFlags.updateFlag(flags, flag, modifier.invoke(current))
        }
    }
}