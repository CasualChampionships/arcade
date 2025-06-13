/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.block

import net.casual.arcade.events.server.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootParams

public data class BlockDropLootEvent(
    val state: BlockState,
    val params: LootParams.Builder,
    var drops: List<ItemStack>
): LevelEvent {
    override val level: ServerLevel
        get() = this.params.level
}