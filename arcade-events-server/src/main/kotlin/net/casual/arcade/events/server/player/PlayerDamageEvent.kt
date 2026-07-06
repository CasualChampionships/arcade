/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

public data class PlayerDamageEvent(
    override val player: ServerPlayer,
    val source: DamageSource,
    var amount: Float,
): CancellableEvent.Simple(), PlayerEvent {
    public companion object {
        public const val PHASE_PRE: Int = BuiltInEventPhases.PRE

        public const val PHASE_POST: Int = BuiltInEventPhases.POST
    }
}