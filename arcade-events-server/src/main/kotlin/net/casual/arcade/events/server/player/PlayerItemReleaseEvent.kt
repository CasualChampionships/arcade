package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

public data class PlayerItemReleaseEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val ticks: Int
): CancellableEvent.Default(), PlayerEvent