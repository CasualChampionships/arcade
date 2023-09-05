package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

data class PlayerItemReleaseEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val ticks: Int
): CancellableEvent.Default(), PlayerEvent