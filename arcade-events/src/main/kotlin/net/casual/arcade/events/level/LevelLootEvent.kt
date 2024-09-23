package net.casual.arcade.events.level

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.RandomizableContainer
import net.minecraft.world.item.ItemStack

public data class LevelLootEvent(
    override val level: ServerLevel,
    val items: List<ItemStack>,
    val container: RandomizableContainer
): LevelEvent