package net.casual.arcade.minigame.managers

import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerTeamJoinEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddAdminEvent
import net.casual.arcade.minigame.events.MinigameRemoveAdminEvent
import net.casual.arcade.minigame.events.MinigameSetPlayingEvent
import net.casual.arcade.minigame.events.MinigameSetSpectatingEvent
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.casual.arcade.utils.PlayerUtils.addToTeam
import net.casual.arcade.utils.PlayerUtils.removeFromTeam
import net.casual.arcade.utils.TeamUtils
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team

public class MinigameTeamManager(
    private val minigame: Minigame
) {
    private var admins: PlayerTeam? = null
    private var spectators: PlayerTeam? = null

    private val eliminated = ReferenceOpenHashSet<PlayerTeam>()

    init {
        this.minigame.events.register<MinigameSetSpectatingEvent> { (_, player) ->
            this.addToSpectatorTeam(player)
        }
        this.minigame.events.register<MinigameSetPlayingEvent> { (_, player) ->
            if (player.team != null && player.team == this.spectators) {
                player.removeFromTeam()
            }
        }
        this.minigame.events.register<MinigameAddAdminEvent> { (_, player) ->
            this.addToAdminTeam(player)
        }
        this.minigame.events.register<MinigameRemoveAdminEvent> { (_, player) ->
            if (player.team != null && player.team == this.admins) {
                player.removeFromTeam()
            }
        }
        this.minigame.events.register<PlayerTeamJoinEvent> { (player, team) ->
            if (team == this.spectators) {
                this.minigame.players.setSpectating(player)
            } else if (team == this.admins) {
                this.minigame.players.addAdmin(player)
            }
        }
    }

    public fun setAdminTeam(team: PlayerTeam) {
        this.admins = team
        for (player in team.getOnlinePlayers()) {
            this.minigame.players.addAdmin(player)
        }
        for (admin in this.minigame.players.admins) {
            this.addToAdminTeam(admin)
        }
    }

    public fun setSpectatorTeam(team: PlayerTeam) {
        this.spectators = team
        for (player in team.getOnlinePlayers()) {
            this.minigame.players.setSpectating(player)
        }
        for (spectator in this.minigame.players.spectating) {
            this.addToSpectatorTeam(spectator)
        }
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
        return this.isTeamEliminated(team) || this.isAdminTeam(team) || this.isSpectatorTeam(team)
    }

    public fun removeEliminatedTeam(team: PlayerTeam) {
        this.eliminated.remove(team)
    }

    public fun hasAdminTeam(): Boolean {
        return this.admins != null
    }

    public fun hasSpectatorTeam(): Boolean {
        return this.spectators != null
    }

    public fun getAdminTeam(): PlayerTeam {
        return this.admins ?: throw IllegalStateException("Cannot get null spectator team")
    }

    public fun getSpectatorTeam(): PlayerTeam {
        return this.spectators ?: throw IllegalStateException("Cannot get null spectator team")
    }

    public fun isAdminTeam(team: Team): Boolean {
        return this.hasAdminTeam() && this.getAdminTeam().isAlliedTo(team)
    }

    public fun isSpectatorTeam(team: Team): Boolean {
        return this.hasSpectatorTeam() && this.getSpectatorTeam().isAlliedTo(team)
    }

    /**
     * This gets all the teams that are playing in the minigame.
     *
     * @return The collection of player teams.
     */
    public fun getOnlineTeams(): Collection<PlayerTeam> {
        return TeamUtils.getTeamsFor(this.minigame.players)
    }

    /**
     * This gets all the playing players teams. This ignores
     * any teams that are marked as eliminated and also does
     * not include spectator or admin teams.
     *
     * @return The collecting of playing players teams.
     */
    public fun getPlayingTeams(): Collection<PlayerTeam> {
        val teams = TeamUtils.getTeamsFor(this.minigame.players.playing)
        val admins = this.admins
        if (admins != null && teams.remove(admins)) {
            ArcadeUtils.logger.warn("MinigameTeamManager.getPlayingTeams included admins")
        }
        val spectators = this.spectators
        if (spectators != null && teams.remove(spectators)) {
            ArcadeUtils.logger.warn("MinigameTeamManager.getPlayingTeams included spectators")
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
        val teams = ReferenceOpenHashSet<PlayerTeam>()
        for (profile in this.minigame.players.allProfiles) {
            teams.add(this.minigame.server.scoreboard.getPlayersTeam(profile.name) ?: continue)
        }
        return teams
    }

    public fun getAllNonSpectatorOrAdminTeams(): Collection<PlayerTeam> {
        val teams = this.getAllTeams() as ReferenceOpenHashSet<PlayerTeam>
        teams.remove(this.admins)
        teams.remove(this.spectators)
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

    internal fun addToSpectatorTeam(player: ServerPlayer) {
        val spectators = this.spectators
        if (player.team == null && spectators != null) {
            player.addToTeam(spectators)
        }
    }

    internal fun addToAdminTeam(player: ServerPlayer) {
        val admins = this.admins
        if (admins != null && (player.team == null || player.team == this.spectators)) {
            player.addToTeam(admins)
        }
    }

    internal fun serialize(): JsonObject {
        val teams = JsonObject()
        teams.addProperty("admins", this.admins?.name)
        teams.addProperty("spectators", this.spectators?.name)
        return teams
    }

    internal fun deserialize(teams: JsonObject, scoreboard: Scoreboard) {
        val admins = teams.stringOrNull("admins")
        if (admins != null) {
            this.admins = scoreboard.getPlayerTeam(admins)
        }
        val spectators = teams.stringOrNull("spectators")
        if (spectators != null) {
            this.spectators = scoreboard.getPlayerTeam(spectators)
        }
    }
}