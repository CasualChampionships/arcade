package net.casual.arcade.events.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

public data class PlayerJoinEvent(
    override val player: ServerPlayer
): CancellableEvent.Typed<Component>(), PlayerEvent {
    public companion object {
        /**
         * This phase is called before the player has initialized,
         * be careful with what you do here.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

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