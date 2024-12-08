package net.casual.arcade.events.level

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