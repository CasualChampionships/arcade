package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

public data class PlayerItemFinishEvent(
    override val player: ServerPlayer,
    val stack: ItemStack
): CancellableEvent.Typed<ItemStack>(), PlayerEvent