package net.casual.arcade.events.entity

import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.NaturalSpawner.SpawnState

data class MobCategorySpawnEvent(
    override val level: ServerLevel,
    val category: MobCategory,
    val pos: ChunkPos,
    val state: SpawnState
): CancellableEvent.Default(), LevelEvent