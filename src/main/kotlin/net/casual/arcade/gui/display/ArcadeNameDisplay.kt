package net.casual.arcade.gui.display

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.utils.NameDisplayUtils.nameDisplay
import net.casual.arcade.utils.ScoreboardUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Scoreboard
import kotlin.random.Random

class ArcadeNameDisplay(title: Component): PlayerUI() {
    private val objective = ScoreboardUtils.dummyObjective(Random.nextInt(Int.MAX_VALUE).toString(16))

    var title: Component = title
        private set

    fun setTitle(title: Component) {
        this.title = title
        this.objective.displayName = title
        for (watching in this.getPlayers()) {
            watching.connection.send(ClientboundSetObjectivePacket(this.objective, ClientboundSetObjectivePacket.METHOD_CHANGE))
        }
    }

    fun setScore(player: ServerPlayer, score: Int) {
        for (watching in this.getPlayers()) {
            watching.connection.send(ClientboundSetScorePacket(ServerScoreboard.Method.CHANGE, this.objective.name, watching.scoreboardName, score))
        }
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.nameDisplay.set(this)
        player.connection.send(ClientboundSetObjectivePacket(this.objective, ClientboundSetObjectivePacket.METHOD_ADD))
        player.connection.send(ClientboundSetDisplayObjectivePacket(Scoreboard.DISPLAY_SLOT_BELOW_NAME, this.objective))
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.nameDisplay.remove()
        player.connection.send(ClientboundSetObjectivePacket(this.objective, ClientboundSetObjectivePacket.METHOD_REMOVE))
        player.connection.send(ClientboundSetDisplayObjectivePacket(Scoreboard.DISPLAY_SLOT_BELOW_NAME, null))
    }
}