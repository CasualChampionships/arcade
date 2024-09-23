package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

public data class PlayerItemUseOnEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val context: UseOnContext
): CancellableEvent.Typed<InteractionResult>(), PlayerEvent