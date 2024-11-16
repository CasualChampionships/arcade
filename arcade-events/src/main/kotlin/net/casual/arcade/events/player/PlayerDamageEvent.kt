package net.casual.arcade.events.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

public data class PlayerDamageEvent(
    override val player: ServerPlayer,
    val source: DamageSource,
    var amount: Float,
): CancellableEvent.Default(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}