package net.casual.arcade.events.player

import net.casual.arcade.events.core.InvokableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

data class PlayerBorderDamageEvent(
    override val player: ServerPlayer,
    val source: DamageSource,
    val amount: Float
): InvokableEvent<Boolean>(), PlayerEvent {
    override fun execute(): Boolean {
        return this.player.hurt(this.source, this.amount)
    }
}