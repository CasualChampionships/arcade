package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext

class PlayerItemUseOnEvent(
    val player: ServerPlayer,
    val stack: ItemStack,
    val context: UseOnContext
): CancellableEvent() {
    private var result: InteractionResult? = null

    fun cancel(result: InteractionResult) {
        this.result = result
        this.cancel();
    }

    fun getResult(): InteractionResult {
        return this.result!!
    }
}