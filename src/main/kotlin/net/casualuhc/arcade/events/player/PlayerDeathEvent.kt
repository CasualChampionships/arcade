package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

class PlayerDeathEvent(
    val player: ServerPlayer,
    val source: DamageSource
): CancellableEvent() {
    public override fun invoke(): Any {
        return this.player.die(this.source)
    }

    public override fun cancel() {
        super.cancel()
    }
}