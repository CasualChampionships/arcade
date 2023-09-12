package net.casual.arcade.gui.display

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.utils.NameDisplayUtils.nameScoreDisplay
import net.casual.arcade.utils.ScoreboardUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.*
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard.Method.CHANGE
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Scoreboard.DISPLAY_SLOT_BELOW_NAME
import kotlin.random.Random

public class ArcadeNameScoreDisplay(title: Component): PlayerUI() {
    private val objective = ScoreboardUtils.dummyObjective(Random.nextInt(Int.MAX_VALUE).toString(16))

    public var title: Component = title
        private set

    public fun setTitle(title: Component) {
        this.title = title
        this.objective.displayName = this.title
        val packet = ClientboundSetObjectivePacket(this.objective, METHOD_CHANGE)
        for (watching in this.getPlayers()) {
            watching.connection.send(packet)
        }
    }

    public fun setScore(player: ServerPlayer, score: Int) {
        val packet = ClientboundSetScorePacket(CHANGE, this.objective.name, player.scoreboardName, score)
        for (watching in this.getPlayers()) {
            watching.connection.send(packet)
        }
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.nameScoreDisplay.set(this)
        player.connection.send(ClientboundSetObjectivePacket(this.objective, METHOD_ADD))
        player.connection.send(ClientboundSetDisplayObjectivePacket(DISPLAY_SLOT_BELOW_NAME, this.objective))
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.nameScoreDisplay.remove()
        player.connection.send(ClientboundSetObjectivePacket(this.objective, METHOD_REMOVE))
        player.connection.send(ClientboundSetDisplayObjectivePacket(DISPLAY_SLOT_BELOW_NAME, null))
    }
}