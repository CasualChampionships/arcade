package net.casual.arcade.events.server.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer

public data class PlayerJumpEvent(
    override val player: ServerPlayer
): CancellableEvent.Default(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}