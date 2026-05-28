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
    return (this as OverridableColor).arcade_getColor() ?: this.color.color
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