/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.entity

import net.casual.arcade.events.BuiltInEventPhases
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public data class EntityTeamJoinEvent(
    override val entity: Entity,
    public val team: PlayerTeam
): EntityEvent {
    public companion object {
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}