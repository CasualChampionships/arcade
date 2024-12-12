package net.casual.arcade.events.server.entity

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

public data class EntityStartTrackingEvent(
    override val entity: Entity,
    override val level: ServerLevel
): EntityEvent