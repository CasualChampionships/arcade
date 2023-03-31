package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

class PlayerDamageEvent(
    val player: ServerPlayer,
    val amount: Float,
    val source: DamageSource
): CancellableEvent() {
    private var newAmount: Float? = null

    fun cancel(amount: Float) {
        this.newAmount = amount
        this.cancel()
    }

    fun getNewAmount(): Float {
        return this.newAmount!!
    }
}