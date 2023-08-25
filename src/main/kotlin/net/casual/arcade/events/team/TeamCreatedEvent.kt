package net.casual.arcade.events.team

import net.casual.arcade.events.core.Event
import net.minecraft.world.scores.PlayerTeam

data class TeamCreatedEvent(
    val team: PlayerTeam
): Event