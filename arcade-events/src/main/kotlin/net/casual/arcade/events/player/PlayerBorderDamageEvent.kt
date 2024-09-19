package net.casual.arcade.events.player

import net.casual.arcade.events.core.InvokableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

public data class PlayerBorderDamageEvent(
    override val player: ServerPlayer,
    val source: DamageSource,
    val amount: Float
): InvokableEvent<Boolean>(false), PlayerEvent {
    override fun execute(): Boolean {
        return this.player.hurt(this.source, this.amount)
    }
}