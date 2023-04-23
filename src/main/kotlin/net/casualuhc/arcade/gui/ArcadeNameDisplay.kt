package net.casualuhc.arcade.gui

import net.casualuhc.arcade.utils.NameDisplayUtils.nameDisplay
import net.casualuhc.arcade.utils.ScoreboardUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.scores.Scoreboard
import kotlin.random.Random

class ArcadeNameDisplay(title: Component) {
    private val connections = HashSet<ServerGamePacketListenerImpl>()
    private val objective = ScoreboardUtils.dummyObjective(Random.nextInt(Int.MAX_VALUE).toString(16))

    var title: Component = title
        private set

    fun setTitle(title: Component) {
        this.title = title
        this.objective.displayName = title
        for (connection in this.connections) {
            connection.send(ClientboundSetObjectivePacket(this.objective, ClientboundSetObjectivePacket.METHOD_CHANGE))
        }
    }

    fun setScore(player: ServerPlayer, score: Int) {
        for (connection in this.connections) {
            connection.send(ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, this.objective.name, player.scoreboardName, score))
        }
    }

    fun addPlayer(player: ServerPlayer) {
        if (this.connections.add(player.connection)) {
            player.nameDisplay.set(this)
            player.connection.send(ClientboundSetObjectivePacket(this.objective, ClientboundSetObjectivePacket.METHOD_ADD))
            player.connection.send(ClientboundSetDisplayObjectivePacket(Scoreboard.DISPLAY_SLOT_BELOW_NAME, this.objective))
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            player.nameDisplay.remove()
            player.connection.send(ClientboundSetObjectivePacket(this.objective, ClientboundSetObjectivePacket.METHOD_REMOVE))
            player.connection.send(ClientboundSetDisplayObjectivePacket(Scoreboard.DISPLAY_SLOT_BELOW_NAME, null))
        }
    }

    fun clearPlayers() {
        for (player in this.getPlayers()) {
            this.removePlayer(player)
        }
    }

    fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }
}