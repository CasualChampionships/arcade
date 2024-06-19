package net.casual.arcade.gui.tab

import com.google.common.collect.Iterables
import net.casual.arcade.resources.font.heads.PlayerHeadComponents
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.colour
import net.casual.arcade.utils.ComponentUtils.italicise
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.PlayerUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import kotlin.math.max

public open class TeamListEntries: PlayerListEntries {
    private val entries = Array(20) { Array(4) { this.getEmptyEntry() } }

    override val size: Int = 80

    final override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        val column = index / 20
        val row = index % 20
        return this.entries[row][column]
    }

    override fun tick(server: MinecraftServer) {
        for (entry in this.entries) {
            entry.fill(this.getEmptyEntry())
        }

        var previousRow = 0
        for (teams in Iterables.partition(this.getTeams(server), 4)) {
            var most = 0
            for ((column, team) in teams.withIndex()) {
                var row = previousRow
                if (row !in this.entries.indices) {
                    break
                }
                this.entries[row++][column] = this.createTeamEntry(server, team)

                val teammates = this.getTeammates(team)
                most = max(teammates.size, most)
                for (username in teammates) {
                    if (row !in this.entries.indices) {
                        break
                    }
                    this.entries[row++][column] = this.createPlayerEntry(server, username, team)
                }
            }
            previousRow += most + 2
        }
    }

    protected open fun getTeammates(team: PlayerTeam): MutableCollection<String> {
        return team.players
    }

    protected open fun getEmptyEntry(): PlayerListEntries.Entry {
        return PlayerListEntries.Entry.HIDDEN
    }

    protected open fun formatTeamName(server: MinecraftServer, team: PlayerTeam): MutableComponent {
        return "-- ".literal().append(team.displayName).append(" --").withStyle(team.color)
    }

    protected open fun createTeamEntry(server: MinecraftServer, team: PlayerTeam): PlayerListEntries.Entry {
        return PlayerListEntries.Entry.fromComponent(this.formatTeamName(server, team))
    }

    protected open fun createPlayerEntry(server: MinecraftServer, username: String, team: PlayerTeam): PlayerListEntries.Entry {
        val head = PlayerHeadComponents.getHeadOrDefault(username)
        val player = PlayerUtils.player(username)
        val name = when {
            player == null -> username.literal().colour(0x808080)
            player.isSpectator -> username.literal().withStyle(team.color).italicise()
            else -> username.literal().withStyle(team.color)
        }
        if (player != null) {
            return PlayerListEntries.Entry.fromComponent(name, player)
        }
        return PlayerListEntries.Entry.fromComponent(
            Component.empty().append(head).append(ComponentUtils.space(2)).append(name)
        )
    }

    protected open fun getTeams(server: MinecraftServer): Collection<PlayerTeam> {
        return server.scoreboard.playerTeams.stream()
            .filter { it.players.isNotEmpty() }
            .sorted(NAME_ORDER)
            .toList()
    }

    public companion object {
        public val NAME_ORDER: Comparator<Team> = Comparator.comparing(Team::getName)
    }
}