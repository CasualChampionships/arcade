/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.BuiltInEventPhases
import net.minecraft.server.level.ServerPlayer

public data class PlayerLeaveEvent(
    override val player: ServerPlayer
): PlayerEvent {
    public companion object {
        /**
         * This phase is called before the player is removed from the server and world.
         *
         * This is the default phase.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        /**
         * This phase is called after the player has been removed from the server and world.
         */
        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}