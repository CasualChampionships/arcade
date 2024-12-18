/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.utils.impl.Location
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.levelgen.structure.Structure
import kotlin.jvm.optionals.getOrNull
import net.casual.arcade.utils.PlayerUtils.teleportTo as teleportPlayerTo

public fun Entity.teleportTo(location: Location, resetCamera: Boolean = true) {
    if (this is ServerPlayer) {
        this.teleportPlayerTo(location, resetCamera)
        return
    }
    this.teleportTo(
        location.level,
        location.x,
        location.y,
        location.z,
        setOf(),
        Mth.wrapDegrees(location.yaw),
        Mth.wrapDegrees(location.pitch),
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