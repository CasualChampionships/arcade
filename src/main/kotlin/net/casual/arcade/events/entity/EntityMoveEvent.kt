package net.casual.arcade.events.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

public data class EntityMoveEvent(
    override val entity: Entity,
    val position: Vec3
): EntityEvent