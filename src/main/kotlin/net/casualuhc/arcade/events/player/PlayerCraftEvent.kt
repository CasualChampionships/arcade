package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

data class PlayerCraftEvent(
    val player: ServerPlayer,
    val stack: ItemStack
): Event()