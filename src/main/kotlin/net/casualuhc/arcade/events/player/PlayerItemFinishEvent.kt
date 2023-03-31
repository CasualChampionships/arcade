package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class PlayerItemFinishEvent(
    val player: ServerPlayer,
    val stack: ItemStack,
    val level: Level
): CancellableEvent() {
    private var finishedItem: ItemStack? = null

    fun cancel(item: ItemStack) {
        this.finishedItem = item
    }

    fun getFinishedItem(): ItemStack {
        return this.finishedItem!!
    }
}