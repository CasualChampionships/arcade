package net.casual.arcade.events.server

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.server.ServerTickEvent.Companion.PHASE_PRE
import net.minecraft.server.MinecraftServer

/**
 * Event that is fired every tick on the server.
 *
 * This has two possible phases when it can be triggered.
 *
 * By default, this event is fired in the [PHASE_PRE] phase.
 */
public data class ServerTickEvent(
    /**
     * The [MinecraftServer] instance that was ticked.
     */
    val server: MinecraftServer
): Event {
    public companion object {
        /**
         * Runs before the server has run the tick.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        /**
         * Runs after the server has run the tick.
         */
        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}