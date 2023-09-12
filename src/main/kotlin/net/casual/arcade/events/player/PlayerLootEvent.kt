package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity

public data class PlayerLootEvent(
    override val player: ServerPlayer,
    val items: List<ItemStack>,
    val container: RandomizableContainerBlockEntity
): PlayerEvent