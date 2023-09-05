package net.casual.arcade.events.player

import net.casual.arcade.events.core.InvokableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

data class PlayerDeathEvent(
    override val player: ServerPlayer,
    val source: DamageSource
): InvokableEvent<Unit>(), PlayerEvent {
    override fun execute() {
        return this.player.die(this.source)
    }
}