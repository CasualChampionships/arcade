/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.NaturalSpawner.SpawnState

public data class MobCategorySpawnEvent(
    override val level: ServerLevel,
    val category: MobCategory,
    val pos: ChunkPos,
    val state: SpawnState
): CancellableEvent.Default(), LevelEvent