package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

data class PlayerItemUseEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val hand: InteractionHand
): CancellableEvent.Typed<InteractionResultHolder<ItemStack>>(), PlayerEvent