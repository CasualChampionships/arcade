package net.casualuhc.arcade.events.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

data class PlayerCraftEvent(
    override val player: ServerPlayer,
    val stack: ItemStack
): PlayerEvent