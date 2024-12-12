package net.casual.arcade.events.server.entity

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.SpawnGroupData

public data class MobSpawnEvent(
    override val level: ServerLevel,
    override val entity: Mob,
    val data: SpawnGroupData?
): CancellableEvent.Default(), EntityEvent