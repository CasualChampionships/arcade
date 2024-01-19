package net.casual.arcade.minigame.managers

import net.casual.arcade.Arcade
import net.casual.arcade.events.minigame.MinigameAddAdminEvent
import net.casual.arcade.events.minigame.MinigameAddSpectatorEvent
import net.casual.arcade.events.minigame.MinigameRemoveAdminEvent
import net.casual.arcade.events.minigame.MinigameRemoveSpectatorEvent
import net.casual.arcade.events.player.PlayerTeamJoinEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.PlayerUtils.addToTeam
import net.casual.arcade.utils.PlayerUtils.removeFromTeam
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.lang.IllegalStateException
import kotlin.collections.HashSet

public class MinigameTeamManager(
    private val minigame: Minigame<*>
) {
    private var admins: PlayerTeam? = null
    private var spectators: PlayerTeam? = null

    private val eliminated = HashSet<PlayerTeam>()

    init {
        this.minigame.events.register<MinigameAddSpectatorEvent> { (_, player) ->
            val spectators = this.spectators
            if (player.team != null && spectators != null) {
                player.addToTeam(spectators)
            }
        }
        this.minigame.events.register<MinigameRemoveSpectatorEvent> { (_, player) ->
            if (player.team != null && player.team == this.spectators) {
                player.removeFromTeam()
            }
        }
        this.minigame.events.register<MinigameAddAdminEvent> { (_, player) ->
            val spectators = this.spectators
            if (spectators != null && (player.team != null || player.team == spectators)) {
                player.addToTeam(spectators)
            }
        }
        this.minigame.events.register<MinigameRemoveAdminEvent> { (_, player) ->
            if (player.team != null && player.team == this.admins) {
                player.removeFromTeam()
            }
        }
        this.minigame.events.register<PlayerTeamJoinEvent> { (player, team) ->
            if (team == this.spectators) {
                this.minigame.makeSpectator(player)
            } else if (team == this.admins) {
                this.minigame.makeAdmin(player)
            }
        }
    }

    public fun setAdminTeam(team: PlayerTeam) {
        this.admins = team
    }

    public fun setSpectatorTeam(team: PlayerTeam) {
        this.spectators = team
    }

    public fun addEliminatedTeam(team: PlayerTeam) {
        this.eliminated.add(team)
    }

    public fun getEliminatedTeams(): Collection<PlayerTeam> {
        return this.eliminated
    }

    public fun isTeamEliminated(team: PlayerTeam): Boolean {
        return this.eliminated.contains(team)
    }

    public fun isTeamIgnored(team: PlayerTeam): Boolean {
        return this.isTeamEliminated(team) || team == this.admins || team == this.spectators
    }

    public fun removeEliminatedTeam(team: PlayerTeam) {
        this.eliminated.remove(team)
    }

    public fun getAdminTeam(): PlayerTeam {
        return this.admins ?: throw IllegalStateException("Cannot get null spectator team")
    }

    public fun getSpectatorTeam(): PlayerTeam {
        return this.spectators ?: throw IllegalStateException("Cannot get null spectator team")
    }

    /**
     * This gets all the teams that are playing in the minigame.
     *
     * @return The collection of player teams.
     */
    public fun getOnlineTeams(): Collection<PlayerTeam> {
        return this.getPlayerTeamsFor(this.minigame.getAllPlayers())
    }

    /**
     * This gets all the playing players teams. This ignores
     * any teams that are marked as eliminated and also does
     * not include spectator or admin teams.
     *
     * @return The collecting of playing players teams.
     */
    public fun getPlayingTeams(): Collection<PlayerTeam> {
        val teams = this.getPlayerTeamsFor(this.minigame.getPlayingPlayers())
        val admins = this.admins
        if (admins != null && teams.remove(admins)) {
            Arcade.logger.warn("MinigameTeamManager.getPlayingTeams included admins")
        }
        val spectators = this.spectators
        if (spectators != null && teams.remove(spectators)) {
            Arcade.logger.warn("MinigameTeamManager.getPlayingTeams included spectators")
        }
        for (ignored in this.eliminated) {
            teams.remove(ignored)
        }
        return teams
    }

    /**
     * This gets all the teams that are playing in the minigame,
     * including offline teams.
     *
     * @return The collection of player teams.
     */
    public fun getAllTeams(): Collection<PlayerTeam> {
        val teams = HashSet<PlayerTeam>()
        for (profile in this.minigame.getAllPlayerProfiles()) {
            teams.add(this.minigame.server.scoreboard.getPlayersTeam(profile.name) ?: continue)
        }
        return teams
    }

    public fun hideNameTags() {
        for (team in this.getAllTeams()) {
            team.nameTagVisibility = Team.Visibility.NEVER
        }
        this.admins?.nameTagVisibility = Team.Visibility.NEVER
        this.spectators?.nameTagVisibility = Team.Visibility.NEVER
    }

    public fun showNameTags() {
        for (team in this.getAllTeams()) {
            team.nameTagVisibility = Team.Visibility.ALWAYS
        }
        this.admins?.nameTagVisibility = Team.Visibility.ALWAYS
        this.spectators?.nameTagVisibility = Team.Visibility.ALWAYS
    }

    private fun getPlayerTeamsFor(players: Collection<ServerPlayer>): HashSet<PlayerTeam> {
        val teams = HashSet<PlayerTeam>()
        for (player in players) {
            teams.add(this.minigame.server.scoreboard.getPlayersTeam(player.scoreboardName) ?: continue)
        }
        return teams
    }
}