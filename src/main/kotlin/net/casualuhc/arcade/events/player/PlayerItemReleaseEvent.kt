package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

data class PlayerItemReleaseEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val level: Level,
    val ticks: Int
): CancellableEvent.Default(), PlayerEvent