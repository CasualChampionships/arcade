package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.RandomizableContainer
import net.minecraft.world.item.ItemStack

public data class PlayerLootEvent(
    override val player: ServerPlayer,
    val items: List<ItemStack>,
    val container: RandomizableContainer
): PlayerEvent