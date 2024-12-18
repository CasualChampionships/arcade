/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.level

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.RandomizableContainer
import net.minecraft.world.item.ItemStack

public data class LevelLootEvent(
    override val level: ServerLevel,
    override val pos: BlockPos,
    val items: List<ItemStack>,
    val container: RandomizableContainer,
): LocatedLevelEvent