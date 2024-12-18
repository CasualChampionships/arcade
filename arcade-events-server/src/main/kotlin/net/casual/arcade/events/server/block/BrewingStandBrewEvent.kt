/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.block

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity
import net.minecraft.world.level.block.state.BlockState

public data class BrewingStandBrewEvent(
    override val level: ServerLevel,
    val pos: BlockPos,
    val state: BlockState,
    val entity: BrewingStandBlockEntity,
): CancellableEvent.Default(), LevelEvent