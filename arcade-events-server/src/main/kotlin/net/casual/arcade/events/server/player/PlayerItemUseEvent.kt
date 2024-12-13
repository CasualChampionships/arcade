package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack

public data class PlayerItemUseEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val hand: InteractionHand
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent