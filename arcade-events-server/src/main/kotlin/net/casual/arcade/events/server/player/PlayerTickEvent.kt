/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.BuiltInEventPhases
import net.minecraft.server.level.ServerPlayer

public data class PlayerTickEvent(
    override val player: ServerPlayer
): PlayerEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}