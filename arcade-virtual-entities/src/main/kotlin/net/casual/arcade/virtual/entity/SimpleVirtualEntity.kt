/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.packet.PacketSender
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.data.PlayerSpecificEntityData
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.utils.*
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.util.Optional
import java.util.UUID
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
    override val attachment: VirtualEntityAttachment,
    override val observers: ObserverTracker = SimpleObserverTracker()
): VirtualEntity {
    override val id: Int = VirtualEntity.getNextEntityId()
    override val uuid: UUID = UUID.randomUUID()
    override var position: VirtualPosition = VirtualPosition.DEFAULT
    override var rotation: VirtualRotation = VirtualRotation.DEFAULT

    private var interaction: (ServerPlayer) -> VirtualEntity.InteractionHandler? = { null }
    private var range = this.type.clientTrackingRange() * 16.0

    private var lastSyncedPos: Vec3? = null
    private var lastSyncedRot: Vec2? = null
    private var lastSyncedHeadRot: Float? = null

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

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        sender.send(this.createSpawnPacket())
        this.sendChangedEntityData(observer, sender)
    }

    override fun sendDespawnPackets(observer: Observer, sender: PacketSender) {
        sender.send(ClientboundRemoveEntitiesPacket(this.id))
    }

    public fun setObservableRange(range: Double) {
        this.range = range
    }

    override fun getObservableRange(): Double {
        return this.range
    }

    public fun setInteractionHandlerProvider(provider: (ServerPlayer) -> VirtualEntity.InteractionHandler?) {
        this.interaction = provider
    }

    override fun getInteractionHandler(player: ServerPlayer): VirtualEntity.InteractionHandler? {
        return this.interaction.invoke(player)
    }

    protected open fun createSpawnPacket(): ClientboundAddEntityPacket {
        val location = this.location()
        val (x, y, z) = if (this.isPassenger) location.position else this.getLastSyncedPosition(location.position)
        val (xRot, yRot) = this.getLastSyncedRotation(location.rotation)
        val headRot = this.getLastSyncedHeadRotation(yRot)
        return ClientboundAddEntityPacket(
            this.id, this.uuid, x, y, z, xRot, yRot, this.type, 0, Vec3.ZERO, headRot.toDouble()
        )
    }

    protected open fun sendChangedEntityData(observer: Observer, sender: PacketSender) {
        val player = observer.asPlayerOrNull()
        val merged = if (player != null) {
            this.data.getChangedEntries(player.uuid, this.data.getChangedBaseEntries())
        } else {
            this.data.getChangedBaseEntries()
        }
        if (merged.isNotEmpty()) {
            sender.send(ClientboundSetEntityDataPacket(this.id, merged))
        }
    }

    protected open fun sendDirtyLocation() {
        val current = this.location()
        val currentRot = current.rotation
        val previousRot = this.getLastSyncedRotation(currentRot)

        if (this.isPassenger) {
            val packet = VirtualEntityPacketUtils.createRotationPacket(this.id, previousRot, currentRot)
            if (packet != null) {
                this.observers.broadcast(packet)
                this.lastSyncedRot = currentRot
            }
            this.sendDirtyHeadRotation(currentRot.y)
            return
        }

        val currentPos = current.position
        val previousPos = this.getLastSyncedPosition(currentPos)
        val packet = VirtualEntityPacketUtils.createMovePacket(
            this.id, previousPos, currentPos, previousRot, currentRot
        )
        if (packet != null) {
            this.observers.broadcast(packet)
            if (VirtualEntityPacketUtils.isEntityPositionPacket(packet)) {
                this.lastSyncedPos = currentPos
            }
            if (VirtualEntityPacketUtils.isEntityRotationPacket(packet)) {
                this.lastSyncedRot = currentRot
            }
        }
        this.sendDirtyHeadRotation(currentRot.y)
    }

    protected fun sendDirtyHeadRotation(yHeadRot: Float) {
        val previousHeadRot = this.getLastSyncedHeadRotation(yHeadRot)
        val packet = VirtualEntityPacketUtils.createHeadRotationPacket(this.id, previousHeadRot, yHeadRot)
        if (packet != null) {
            this.observers.broadcast(packet)
        }
    }

    protected open fun sendDirtyEntityData() {
        val base = this.data.getDirtyBaseEntries()
        this.observers.broadcast { observer ->
            val player = observer.asPlayerOrNull()
            val overridden = if (player != null) this.data.getDirtyEntries(player.uuid, base) else base
            if (overridden.isNotEmpty()) {
                observer.send(ClientboundSetEntityDataPacket(this.id, overridden))
            }
        }
    }

    protected fun getLastSyncedPosition(current: Vec3): Vec3 {
        if (this.lastSyncedPos == null) {
            this.lastSyncedPos = current
        }
        return this.lastSyncedPos!!
    }

    protected fun getLastSyncedRotation(current: Vec2): Vec2 {
        if (this.lastSyncedRot == null) {
            this.lastSyncedRot = current
        }
        return this.lastSyncedRot!!
    }

    protected fun getLastSyncedHeadRotation(current: Float): Float {
        if (this.lastSyncedHeadRot == null) {
            this.lastSyncedHeadRot = current
        }
        return this.lastSyncedHeadRot!!
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
        this.setDataEntry(EntityDataAccessors.CUSTOM_NAME, Optional.ofNullable(name))
    }

    public fun setCustomNameFor(observer: ServerPlayer, name: Component?) {
        this.setDataEntryFor(observer, EntityDataAccessors.CUSTOM_NAME, Optional.ofNullable(name))
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
            val current = base.value.toInt() and flag != 0
            this.modifyFlagEntryFor(observer, flag, current)
        }
    }

    protected fun modifyFlagEntry(flag: Int, modifier: (Boolean) -> Boolean) {
        this.modifyDataEntry(EntityDataAccessors.SHARED_FLAGS) { flags ->
            val current = flags.toInt() and flag != 0
            EntityDataSharedFlags.updateFlag(flags, flag, modifier.invoke(current))
        }
    }

    public companion object {
        public fun typed(type: EntityType<*>): (VirtualEntityAttachment, ObserverTracker) -> SimpleVirtualEntity {
            return { attachment, observers -> SimpleVirtualEntity(type, attachment, observers) }
        }
    }
}