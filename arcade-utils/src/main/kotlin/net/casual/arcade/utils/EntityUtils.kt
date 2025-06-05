/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.util.mixins.ChunkMapAccessor
import net.casual.arcade.util.mixins.TrackedEntityAccessor
import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.network.protocol.game.ClientboundExplodePacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ChunkMap
import net.minecraft.server.level.ServerEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Relative
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer
import kotlin.jvm.optionals.getOrNull

public fun MinecraftServer.findEntity(uuid: UUID): Entity? {
    return this.allLevels.firstNotNullOfOrNull { it.getEntity(uuid) }
}

public fun <T: Any> EntityDataAccessor<T>.createValue(value: T): SynchedEntityData.DataValue<T> {
    return SynchedEntityData.DataValue.create(this, value)
}

public fun Entity.addVelocityAndMark(deltaX: Double, deltaY: Double, deltaZ: Double) {
    this.push(deltaX, deltaY, deltaZ)
    this.hurtMarked = true
}

public fun Entity.setVelocityAndMark(deltaX: Double, deltaY: Double, deltaZ: Double) {
    this.setDeltaMovement(deltaX, deltaY, deltaZ)
    this.hurtMarked = true
}

public fun Entity.addVelocitySmooth(deltaX: Double, deltaY: Double, deltaZ: Double) {
    if (this !is ServerPlayer) {
        return this.addVelocityAndMark(deltaX, deltaY, deltaZ)
    }
    this.addVelocitySmooth(Vec3(deltaX, deltaY, deltaZ))
}

public fun Entity.addVelocitySmooth(deltas: Vec3) {
    if (this !is ServerPlayer) {
        return this.addVelocityAndMark(deltas.x, deltas.y, deltas.z)
    }
    this.addVelocitySmooth(deltas)
}

public fun ServerPlayer.addVelocitySmooth(deltas: Vec3) {
    this.connection.send(ClientboundExplodePacket(
        Vec3(0.0, Int.MAX_VALUE.toDouble(), 0.0),
        Optional.of(deltas),
        ParticleTypes.CRIT,
        SoundEvents.NOTE_BLOCK_BASEDRUM
    ))
}

public fun Entity.teleportTo(location: LocationWithLevel<out ServerLevel>, resetCamera: Boolean = true) {
    this.teleportTo(
        location.level,
        location.x,
        location.y,
        location.z,
        setOf(),
        Mth.wrapDegrees(location.yRot),
        Mth.wrapDegrees(location.xRot),
        resetCamera,
    )
}

public fun Entity.teleportTo(position: Vec3) {
    this.teleportTo(
        this.level() as ServerLevel,
        position.x,
        position.y,
        position.z,
        setOf(Relative.X_ROT, Relative.Y_ROT),
        0.0F,
        0.0F,
        false
    )
}

public fun Entity.teleportTo(location: Location, resetCamera: Boolean = true) {
    this.teleportTo(location.with(this.level() as ServerLevel), resetCamera)
}

public fun Entity.getTrackingPlayers(): List<ServerPlayer> {
    val tracked = this.getTrackedEntity() ?: return listOf()
    return (tracked as TrackedEntityAccessor).seenBy.map { it.player }
}

public fun Entity.getServerEntity(): ServerEntity? {
    val tracked = this.getTrackedEntity() ?: return null
    return (tracked as TrackedEntityAccessor).serverEntity
}

public fun Entity.isInStructure(key: ResourceKey<Structure>): Boolean {
    val access = this.level().registryAccess()
    val structure = access.lookup(Registries.STRUCTURE).getOrNull()?.getOptional(key)?.getOrNull() ?: return false
    return this.isInStructure(structure)
}

public fun Entity.isInStructure(structure: Structure): Boolean {
    val level = this.level()
    if (level !is ServerLevel) {
        return false
    }
    return level.structureManager().getStructureWithPieceAt(this.blockPosition(), structure).isValid
}

public fun <T: Entity> EntityType<T>.spawn(
    location: LocationWithLevel<ServerLevel>,
    reason: EntitySpawnReason = EntitySpawnReason.EVENT
): T? {
    val consumer = Consumer<T> { entity ->
        entity.snapTo(location.position, location.yRot, location.xRot)
        entity.yHeadRot = location.yRot
        entity.setYBodyRot(location.yRot)
    }
    return this.spawn(location.level, consumer, BlockPos.containing(location.position), reason, false, false)
}

private fun Entity.getTrackedEntity(): ChunkMap.TrackedEntity? {
    return ((this.level() as ServerLevel).chunkSource.chunkMap as ChunkMapAccessor).entityMap.get(this.id)
}

public object SynchedDataUtils {
    public fun <T> replaceSynchedData(
        data: MutableList<SynchedEntityData.DataValue<*>>,
        accessor: EntityDataAccessor<T>,
        replacer: (T, Consumer<SynchedEntityData.DataValue<*>>) -> Unit
    ) {
        val iter = data.listIterator()
        for (entry in iter) {
            if (entry.id == accessor.id) {
                iter.remove()
                @Suppress("UNCHECKED_CAST")
                replacer.invoke(entry.value as T, iter::add)
                break
            }
        }
    }
}