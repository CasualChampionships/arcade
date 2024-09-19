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

public fun Entity.teleportTo(location: Location) {
    if (this is ServerPlayer) {
        this.teleportPlayerTo(location)
        return
    }
    this.teleportTo(
        location.level,
        location.x,
        location.y,
        location.z,
        setOf(),
        Mth.wrapDegrees(location.yaw),
        Mth.wrapDegrees(location.pitch)
    )
}

public fun Entity.isInStructure(key: ResourceKey<Structure>): Boolean {
    val access = this.level().registryAccess()
    val structure = access.registry(Registries.STRUCTURE).getOrNull()?.get(key) ?: return false
    return this.isInStructure(structure)
}

public fun Entity.isInStructure(structure: Structure): Boolean {
    val level = this.level()
    if (level !is ServerLevel) {
        return false
    }
    return level.structureManager().getStructureWithPieceAt(this.blockPosition(), structure).isValid
}