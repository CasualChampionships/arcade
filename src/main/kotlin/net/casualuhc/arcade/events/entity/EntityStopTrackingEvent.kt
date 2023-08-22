package net.casualuhc.arcade.events.entity

import net.casualuhc.arcade.events.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

data class EntityStopTrackingEvent(
    override val level: ServerLevel,
    val entity: Entity
): LevelEvent