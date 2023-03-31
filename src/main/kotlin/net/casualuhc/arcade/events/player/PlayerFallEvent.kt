package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

class PlayerFallEvent(
    val player: ServerPlayer,
    val damage: Int,
    val distance: Float,
    val multiplier: Float,
    val source: DamageSource
): CancellableEvent() {
    private var newDamage: Int? = null

    fun cancel(damage: Int) {
        this.newDamage = damage
        super.cancel()
    }

    fun getNewDamage(): Int {
        return this.newDamage!!
    }
}