package net.casual.arcade.events.entity

import net.casual.arcade.events.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

data class EntityStartTrackingEvent(
    override val level: ServerLevel,
    val entity: Entity
): LevelEvent