package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.InvokableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

class PlayerDeathEvent(
    val player: ServerPlayer,
    val source: DamageSource
): InvokableEvent<Unit>() {
    override fun execute() {
        return this.player.die(this.source)
    }
}