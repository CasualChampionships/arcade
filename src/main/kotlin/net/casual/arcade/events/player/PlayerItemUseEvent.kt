package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack

public data class PlayerItemUseEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val hand: InteractionHand
): CancellableEvent.Typed<InteractionResultHolder<ItemStack>>(), PlayerEvent