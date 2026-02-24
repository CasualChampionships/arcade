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
            colors.remove(TeamColor.from(team.color))
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
                val teamName = "${color.name}$animal"
                val team = scoreboard.getOrCreateTeam(teamName)
                if (team.players.isEmpty()) {
                    team.color = color.formatting
                    team.displayName = Component.literal("${color.name} $animal").withStyle(color.formatting)
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
                val teamName = "${color.name}$animal"
                val team = scoreboard.getPlayerTeam(teamName) ?: continue
                scoreboard.removePlayerTeam(team)
            }
        }
    }

    private fun loadTeamAnimals(): HashMultimap<TeamColor, String> {
        val map = HashMultimap.create<TeamColor, String>()
        map.putAll(TeamColor.Black, listOf("Bats", "Bears", "Buffaloes"))
        map.putAll(TeamColor.Navy, listOf("Narwhals"))
        map.putAll(TeamColor.Green, listOf("Gorillas", "Geese", "Geckos"))
        map.putAll(TeamColor.Teal, listOf("Turkeys", "Turtles", "Tigers"))
        map.putAll(TeamColor.Red, listOf("Rhinos", "Rabbits", "Robins"))
        map.putAll(TeamColor.Purple, listOf("Pandas", "Penguins"))
        map.putAll(TeamColor.Orange, listOf("Ocelots", "Owls"))
        map.putAll(TeamColor.Silver, listOf("Spiders", "Sharks"))
        map.putAll(TeamColor.Gray, listOf("Goats"))
        map.putAll(TeamColor.Blue, listOf("Beavers", "Butterflies", "Beetles"))
        map.putAll(TeamColor.Lime, listOf("Lizards", "Leopards"))
        map.putAll(TeamColor.Aqua, listOf("Armadillos", "Axolotls"))
        map.putAll(TeamColor.Crimson, listOf("Crocodiles", "Cats"))
        map.putAll(TeamColor.Pink, listOf("Parrots", "Peacocks"))
        map.putAll(TeamColor.Yellow, listOf("Yaks"))
        map.putAll(TeamColor.White, listOf("Whales", "Wolves"))
        return map
    }
}