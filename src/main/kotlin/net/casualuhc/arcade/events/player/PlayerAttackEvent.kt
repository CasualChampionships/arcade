package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

class PlayerAttackEvent(
    val player: ServerPlayer,
    val target: Entity,
    val damage: Float
): CancellableEvent() {
    private var newDamage: Float? = null

    fun cancel(damage: Float) {
        this.newDamage = damage
        this.cancel()
    }

    fun getNewDamage(): Float {
        return this.newDamage!!
    }
}