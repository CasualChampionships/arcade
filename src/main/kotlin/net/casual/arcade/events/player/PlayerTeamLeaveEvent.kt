package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

public data class PlayerTeamLeaveEvent(
    override val player: ServerPlayer,
    val team: PlayerTeam
): PlayerEvent