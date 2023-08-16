package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

data class PlayerItemUseOnEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val context: UseOnContext
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent