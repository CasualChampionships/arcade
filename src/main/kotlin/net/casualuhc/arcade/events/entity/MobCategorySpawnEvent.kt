package net.casualuhc.arcade.events.entity

import net.casualuhc.arcade.events.core.CancellableEvent
import net.casualuhc.arcade.events.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.ChunkPos

data class MobCategorySpawnEvent(
    override val level: ServerLevel,
    val category: MobCategory,
    val pos: ChunkPos
): CancellableEvent.Default(), LevelEvent