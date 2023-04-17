package net.casualuhc.arcade.utils

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.PlayerCreatedEvent
import net.casualuhc.arcade.events.player.PlayerLeaveEvent
import net.casualuhc.arcade.events.player.PlayerTickEvent
import net.casualuhc.arcade.gui.PlayerSidebarExtension
import net.casualuhc.arcade.utils.PlayerUtils.addExtension
import net.casualuhc.arcade.utils.PlayerUtils.getExtension
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard.Method
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.criteria.ObjectiveCriteria.DUMMY
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType.INTEGER

object SidebarUtils {
    const val MAX_SIZE = 14

    private val scoreboard = Scoreboard()
    private val objectiveName = "\$DummyObjective"
    private val objective = Objective(this.scoreboard, this.objectiveName, DUMMY, Component.empty(), INTEGER)
    private val teamName = "\$DummyTeam"
    private val teams = ArrayList<PlayerTeam>(16)
    private val players = ArrayList<String>(16)

    internal val ServerPlayer.sidebar
        get() = this.getExtension(PlayerSidebarExtension::class.java)

    init {
        for (i in 0..15) {
            val player = ChatFormatting.RESET.toString().repeat(i)
            val team = PlayerTeam(this.scoreboard, this.teamName + i)

            team.players.add(player)
            this.teams.add(team)
            this.players.add(player)
        }
    }

    internal fun sendSetObjectivePacket(player: ServerPlayer, method: Int, title: Component? = null) {
        if (title !== null) {
            this.objective.displayName = title
        }
        val packet = ClientboundSetObjectivePacket(this.objective, method)
        player.connection.send(packet)
    }

    internal fun sendSetScorePacket(player: ServerPlayer, index: Int, method: Method) {
        player.connection.send(ClientboundSetScorePacket(method, this.objectiveName, this.players[index], index))
    }

    internal fun sendSetDisplayPacket(player: ServerPlayer, remove: Boolean) {
        player.connection.send(ClientboundSetDisplayObjectivePacket(Scoreboard.DISPLAY_SLOT_SIDEBAR, if (remove) null else this.objective))
    }

    internal fun sendPlayerTeamUpdatePacket(player: ServerPlayer, index: Int, initial: Boolean, prefix: Component)  {
        val team = this.teams[index]
        team.playerPrefix = prefix
        player.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, initial))
    }

    internal fun sendPlayerTeamRemovePacket(player: ServerPlayer, index: Int) {
        player.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(this.teams[index]))
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerSidebarExtension(player))
        }
        GlobalEventHandler.register<PlayerLeaveEvent> { (player) ->
            player.sidebar.disconnect()
        }
        GlobalEventHandler.register<PlayerTickEvent> { (player) ->
            player.sidebar.tick()
        }
    }
}