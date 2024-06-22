package net.casual.arcade.events.entity

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

public data class EntityStopTrackingEvent(
    override val entity: Entity,
    override val level: ServerLevel,
): EntityEvent