package net.casual.arcade.events.entity

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.SpawnGroupData

public data class MobSpawnEvent(
    override val level: ServerLevel,
    override val entity: Mob,
    val data: SpawnGroupData?
): CancellableEvent.Default(), EntityEvent