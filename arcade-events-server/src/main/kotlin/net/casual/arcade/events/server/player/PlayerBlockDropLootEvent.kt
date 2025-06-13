/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

public data class PlayerBlockDropLootEvent(
    override val player: ServerPlayer,
    override val level: ServerLevel,
    val state: BlockState,
    val pos: BlockPos,
    val tool: ItemStack,
    var drops: List<ItemStack>
): PlayerEvent, LevelEvent
