package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity

class PlayerLootEvent(
    val player: ServerPlayer,
    val items: List<ItemStack>,
    val container: RandomizableContainerBlockEntity
): Event()