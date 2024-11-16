package net.casual.arcade.events.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer

public data class PlayerHealEvent(
    override val player: ServerPlayer,
    val healAmount: Float
): CancellableEvent.Default(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}