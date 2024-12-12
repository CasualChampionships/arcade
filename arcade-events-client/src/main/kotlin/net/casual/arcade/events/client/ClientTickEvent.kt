package net.casual.arcade.events.client

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.Event
import net.minecraft.client.Minecraft

public data class ClientTickEvent(
    val client: Minecraft
): Event {
    public companion object {
        /**
         * Runs before the client has run the tick.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        /**
         * Runs after the client has run the tick.
         */
        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}
