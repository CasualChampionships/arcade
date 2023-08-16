package net.casualuhc.arcade.events.team

import net.casualuhc.arcade.events.core.Event
import net.minecraft.world.scores.PlayerTeam

data class TeamCreatedEvent(
    val team: PlayerTeam
): Event