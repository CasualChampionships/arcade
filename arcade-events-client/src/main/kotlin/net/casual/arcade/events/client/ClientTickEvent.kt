/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client

import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.common.Event
import net.minecraft.client.Minecraft

public data class ClientTickEvent(
    val minecraft: Minecraft
): Event {
    public companion object {
        /**
         * Runs before the client has run the tick.
         */
        public const val PHASE_PRE: Int = BuiltInEventPhases.PRE

        /**
         * Runs after the client has run the tick.
         */
        public const val PHASE_POST: Int = BuiltInEventPhases.POST
    }
}
