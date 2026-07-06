/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.minecraft.server.level.ServerPlayer

public data class PlayerJumpEvent(
    override val player: ServerPlayer
): CancellableEvent.Simple(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE: Int = BuiltInEventPhases.PRE

        public const val PHASE_POST: Int = BuiltInEventPhases.POST
    }
}