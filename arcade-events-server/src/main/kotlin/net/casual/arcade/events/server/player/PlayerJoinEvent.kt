package net.casual.arcade.events.server.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

public data class PlayerJoinEvent(
    override val player: ServerPlayer
): CancellableEvent.Typed<Component>(), PlayerEvent {
    /**
     * This will delay the join from just after the [PHASE_INITIALIZED] phase
     * to just after the [PHASE_POST] phase.
     *
     * This cannot be set in the [PHASE_POST] phase as it is too late.
     */
    var delayJoinMessage: Boolean = false

    public companion object {
        /**
         * This phase is called after the player's [ServerGamePacketListenerImpl]
         * has been initialized. They haven't been added to the player list yet.
         */
        public const val PHASE_INITIALIZED: String = "initialized"

        /**
         * This phase is called after the player has been added to the player list.
         *
         * This is the default phase for this event.
         */
        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}