package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class PlayerItemUseEvent(
    val player: ServerPlayer,
    val stack: ItemStack,
    val level: Level,
    val hand: InteractionHand
): CancellableEvent() {
    private var result: InteractionResultHolder<ItemStack>? = null

    fun cancel(result: InteractionResultHolder<ItemStack>) {
        this.result = result
        this.cancel()
    }

    fun getResult(): InteractionResultHolder<ItemStack> {
        return this.result!!
    }
}