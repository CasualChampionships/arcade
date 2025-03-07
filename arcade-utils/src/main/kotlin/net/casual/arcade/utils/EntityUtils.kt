/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.core.registries.Registries
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.UUID
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

public fun Entity.teleportTo(location: LocationWithLevel<ServerLevel>, resetCamera: Boolean = true) {
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