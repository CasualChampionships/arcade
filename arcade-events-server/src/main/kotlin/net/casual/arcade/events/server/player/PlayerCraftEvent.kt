package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

public data class PlayerCraftEvent(
    override val player: ServerPlayer,
    val stack: ItemStack
): PlayerEvent