package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

class PlayerBorderDamageEvent(
    val player: ServerPlayer,
    val source: DamageSource,
    val amount: Float
): CancellableEvent() {
    public override fun invoke(): Boolean {
        return this.player.hurt(this.source, this.amount)
    }

    public override fun cancel() {
        super.cancel()
    }
}