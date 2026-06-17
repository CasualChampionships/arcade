/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.scoreboard

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import net.casual.arcade.util.ducks.OverridableColor
import net.casual.arcade.util.mixins.teams.ServerScoreboardAccessor
import net.casual.arcade.utils.server.player
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.TeamColor
import kotlin.jvm.optionals.getOrNull

public val PlayerTeam.server: MinecraftServer
    get() = (this.scoreboard as? ServerScoreboardAccessor)?.arcade_getServer()
        ?: throw IllegalStateException("Tried to access team server when there was none!")

public fun PlayerTeam.add(entity: Entity) {
    this.scoreboard.addPlayerToTeam(entity.scoreboardName, this)
}

public fun Iterable<Entity>.getTeams(): MutableSet<PlayerTeam> {
    val teams = HashSet<PlayerTeam>()
    for (entity in this) {
        val team = entity.team ?: continue
        teams.add(team)
    }
    return teams
}

public fun Iterable<Entity>.getMappedTeams(): Multimap<PlayerTeam, Entity> {
    val teams = LinkedHashMultimap.create<PlayerTeam, Entity>()
    for (entity in this) {
        val team = entity.team ?: continue
        teams.put(team, entity)
    }
    return teams
}

public fun Scoreboard.getOrCreateTeam(name: String): PlayerTeam {
    return this.getPlayerTeam(name) ?: this.addPlayerTeam(name)
}

public fun PlayerTeam.getOnlinePlayers(): List<ServerPlayer> {
    val server = this.server
    return this.players.mapNotNull { username -> server.player(username) }
}

public fun PlayerTeam.getOnlineCount(): Int {
    val server = this.server
    return this.players.count { username -> server.player(username) != null }
}

public fun PlayerTeam.setHexColor(color: Int?) {
    (this as OverridableColor).arcade_setColor(color)
}

public fun PlayerTeam.getHexColor(): Int? {
    return (this as OverridableColor).arcade_getColor() ?: this.color.map(TeamColor::rgb)?.getOrNull()
}

public fun MutableComponent.color(team: PlayerTeam?): MutableComponent {
    if (team == null) {
        return this
    }
    val color = team.getHexColor()
    if (color != null) {
        this.withColor(color)
    }
    return this
}

public fun TeamColor.getPrettyName(): String {
    return when (this) {
        TeamColor.BLACK -> "Black"
        TeamColor.DARK_BLUE -> "Navy"
        TeamColor.DARK_GREEN -> "Green"
        TeamColor.DARK_AQUA -> "Teal"
        TeamColor.DARK_RED -> "Red"
        TeamColor.DARK_PURPLE -> "Purple"
        TeamColor.GOLD -> "Orange"
        TeamColor.GRAY -> "Silver"
        TeamColor.DARK_GRAY -> "Gray"
        TeamColor.BLUE -> "Blue"
        TeamColor.GREEN -> "Lime"
        TeamColor.AQUA -> "Aqua"
        TeamColor.RED -> "Crimson"
        TeamColor.LIGHT_PURPLE -> "Pink"
        TeamColor.YELLOW -> "Yellow"
        TeamColor.WHITE -> "White"
    }
}