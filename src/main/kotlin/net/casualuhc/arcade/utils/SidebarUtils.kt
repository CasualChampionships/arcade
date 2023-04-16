package net.casualuhc.arcade.utils

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.player.PlayerCreatedEvent
import net.casualuhc.arcade.events.player.PlayerLeaveEvent
import net.casualuhc.arcade.events.player.PlayerTickEvent
import net.casualuhc.arcade.mixin.scoreboard.ClientboundSetDisplayObjectivePacketAccessor
import net.casualuhc.arcade.mixin.scoreboard.ClientboundSetObjectivePacketAccessor
import net.casualuhc.arcade.mixin.scoreboard.ParametersAccessor
import net.casualuhc.arcade.scoreboards.PlayerSidebarExtension
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
import net.minecraft.world.scores.criteria.ObjectiveCriteria

object SidebarUtils {
    const val MAX_SIZE = 14

    private val scoreboard = Scoreboard()
    private val objectiveName = "\$DummyObjective"
    private val objective = Objective(scoreboard, objectiveName, ObjectiveCriteria.DUMMY, Component.empty(), ObjectiveCriteria.RenderType.INTEGER)
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
        val packet = ClientboundSetObjectivePacket(this.objective, method)
        if (title !== null) {
            @Suppress("KotlinConstantConditions")
            (packet as ClientboundSetObjectivePacketAccessor).setDisplayName(title)
        }
        player.connection.send(packet)
    }

    internal fun sendSetScorePacket(player: ServerPlayer, index: Int, method: Method) {
        player.connection.send(ClientboundSetScorePacket(method, this.objectiveName, this.players[index], index))
    }

    internal fun sendSetDisplayPacket(player: ServerPlayer, remove: Boolean) {
        val packet = ClientboundSetDisplayObjectivePacket(Scoreboard.DISPLAY_SLOT_SIDEBAR, null)
        if (!remove) {
            @Suppress("KotlinConstantConditions")
            (packet as ClientboundSetDisplayObjectivePacketAccessor).setObjectiveName(this.objectiveName)
        }
        player.connection.send(packet)
    }

    internal fun sendPlayerTeamUpdatePacket(player: ServerPlayer, index: Int, initial: Boolean, prefix: Component)  {
        val packet = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(this.teams[index], initial)
        (packet.parameters.orElseThrow() as ParametersAccessor).setPrefix(prefix)
        player.connection.send(packet)
    }

    internal fun sendPlayerTeamRemovePacket(player: ServerPlayer, index: Int) {
        player.connection.send(ClientboundSetPlayerTeamPacket.createRemovePacket(this.teams[index]))
    }

    internal fun registerEvents() {
        EventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerSidebarExtension(player))
        }
        EventHandler.register<PlayerLeaveEvent> { (player) ->
            player.sidebar.disconnect()
        }
        EventHandler.register<PlayerTickEvent> { (player) ->
            player.sidebar.tick()
        }
    }
}