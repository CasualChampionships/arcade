package net.casualuhc.arcade.events.entity

import net.casualuhc.arcade.events.core.CancellableEvent
import net.casualuhc.arcade.events.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.SpawnGroupData

data class MobSpawnEvent(
    override val level: ServerLevel,
    val mob: Mob,
    val data: SpawnGroupData?
): CancellableEvent.Default(), LevelEvent