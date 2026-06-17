/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.scoreboard

import com.google.common.collect.HashMultimap
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import net.minecraft.world.scores.TeamColor
import java.util.*

public object RandomizedTeams {
    private val animals = this.loadTeamAnimals()

    @JvmStatic
    public fun createRandomTeams(
        scoreboard: Scoreboard,
        entities: Collection<Entity>,
        teamSize: Int,
        friendlyFire: Boolean,
        collision: Team.CollisionRule
    ): Collection<PlayerTeam>? {
        val teams = entities.shuffled().chunked(teamSize)

        val generated = ArrayList<PlayerTeam>()
        val colors = TeamColor.entries.toMutableSet()
        for (teammates in teams) {
            var team: PlayerTeam? = null
            var i = 0
            while (team == null) {
                team = this.getUnusedRandomTeam(scoreboard, colors)
                if (i++ > 20) {
                    return null
                }
            }
            team.color.ifPresent(colors::remove)
            team.isAllowFriendlyFire = friendlyFire
            team.collisionRule = collision
            for (entity in teammates) {
                team.add(entity)
            }
            generated.add(team)
        }
        return generated
    }

    @JvmStatic
    public fun getUnusedRandomTeam(scoreboard: Scoreboard, allowable: Collection<TeamColor>): PlayerTeam? {
        val colors = allowable.shuffled()
        for (color in colors) {
            for (animal in this.animals[color].shuffled()) {
                val teamName = "${color.getPrettyName()}$animal"
                val team = scoreboard.getOrCreateTeam(teamName)
                if (team.players.isEmpty()) {
                    team.color = Optional.of(color)
                    team.displayName = Component.literal("${color.getPrettyName()} $animal").withColor(color.textColor())
                    team.setPlayerPrefix(team.formattedDisplayName.append(" "))
                    return team
                }
            }
        }
        return null
    }

    @JvmStatic
    public fun deleteAllRandomTeams(scoreboard: Scoreboard) {
        for (color in TeamColor.entries) {
            for (animal in this.animals[color]) {
                val teamName = "${color.getPrettyName()}$animal"
                val team = scoreboard.getPlayerTeam(teamName) ?: continue
                scoreboard.removePlayerTeam(team)
            }
        }
    }

    private fun loadTeamAnimals(): HashMultimap<TeamColor, String> {
        val map = HashMultimap.create<TeamColor, String>()
        map.putAll(TeamColor.BLACK, listOf("Bats", "Bears", "Buffaloes"))
        map.putAll(TeamColor.DARK_BLUE, listOf("Narwhals"))
        map.putAll(TeamColor.DARK_GREEN, listOf("Gorillas", "Geese", "Geckos"))
        map.putAll(TeamColor.DARK_AQUA, listOf("Turkeys", "Turtles", "Tigers"))
        map.putAll(TeamColor.DARK_RED, listOf("Rhinos", "Rabbits", "Robins"))
        map.putAll(TeamColor.DARK_PURPLE, listOf("Pandas", "Penguins"))
        map.putAll(TeamColor.GOLD, listOf("Ocelots", "Owls"))
        map.putAll(TeamColor.GRAY, listOf("Spiders", "Sharks"))
        map.putAll(TeamColor.DARK_GRAY, listOf("Goats"))
        map.putAll(TeamColor.BLUE, listOf("Beavers", "Butterflies", "Beetles"))
        map.putAll(TeamColor.GREEN, listOf("Lizards", "Leopards"))
        map.putAll(TeamColor.AQUA, listOf("Armadillos", "Axolotls"))
        map.putAll(TeamColor.RED, listOf("Crocodiles", "Cats"))
        map.putAll(TeamColor.LIGHT_PURPLE, listOf("Parrots", "Peacocks"))
        map.putAll(TeamColor.YELLOW, listOf("Yaks"))
        map.putAll(TeamColor.WHITE, listOf("Whales", "Wolves"))
        return map
    }
}