package net.casualuhc.arcade.scoreboards

import net.casualuhc.arcade.utils.TextUtils.toFormattedString
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.METHOD_ADD
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.METHOD_CHANGE
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.server.ServerScoreboard.Method
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Scoreboard.DISPLAY_SLOT_SIDEBAR
import net.minecraft.world.scores.criteria.ObjectiveCriteria
import kotlin.random.Random

class SimpleSidebar(
    private val owner: ServerPlayer
) {
    private val objective = createEmptyObjective()
    private val strings = ArrayList<String>(MAX_SIZE)
    private val rows = ArrayList<Component>(MAX_SIZE)

    init {
        this.sendObjectivePacket(METHOD_ADD)
    }

    fun show() {
        this.sendDisplayPacket(false)
    }

    fun hide() {
        this.sendDisplayPacket(true)
    }

    fun size(): Int {
        return this.rows.size
    }

    fun setName(name: Component) {
        this.objective.displayName = name
        this.sendObjectivePacket(METHOD_CHANGE)
    }

    fun getRow(index: Int): Component {
        this.checkBounds(index, this.size() - 1)
        return this.rows[index]
    }

    fun addRow(component: Component) {
        this.addRow(this.size(), component)
    }

    // Warning: This will **NOT** work with translatable components
    fun addRow(index: Int, component: Component) {
        require(this.size() < MAX_SIZE) { "Cannot add more rows, already at max size: $MAX_SIZE" }
        this.checkBounds(index, this.size())

        val string = this.uniqueString(component.toFormattedString())
        this.rows.add(index, component)
        this.strings.add(index, string)

        for (i in index until this.size()) {
            this.sendScorePacket(i, Method.CHANGE, this.strings[i])
        }
    }

    fun setRow(index: Int, component: Component) {
        this.checkBounds(index, this.size() - 1)
        val previous = this.strings[index]

        var replacement = component.toFormattedString()
        if (replacement == previous) {
            return
        }
        replacement = this.uniqueString(replacement)
        this.rows[index] = component
        this.strings[index] = replacement

        this.sendScorePacket(index, Method.REMOVE, previous)
        this.sendScorePacket(index, Method.CHANGE, replacement)
    }

    fun removeRow(index: Int) {
        this.checkBounds(index, this.size() - 1)

        this.rows.removeAt(index)
        val previous = this.strings.removeAt(index)
        this.sendScorePacket(index, Method.REMOVE, previous)

        for (i in index until this.size()) {
            this.sendScorePacket(i, Method.CHANGE, this.strings[i])
        }
    }

    private fun checkBounds(index: Int, upper: Int) {
        require(index in 0..upper) { "Row index $index out of bounds! Must between 0 and $upper" }
    }

    private fun uniqueString(string: String): String {
        // We cannot have 2 equal strings, this is because the client
        // uses a map to store the 'player' names.
        if (!this.strings.contains(string)) {
            return string
        }
        // We work around this by appending formatting which is invisible
        return this.uniqueString(string + ChatFormatting.RESET.toString())
    }

    private fun sendObjectivePacket(method: Int) {
        this.owner.connection.send(ClientboundSetObjectivePacket(this.objective, method))
    }

    private fun sendDisplayPacket(hidden: Boolean) {
        val objective = if (hidden) null else this.objective
        this.owner.connection.send(ClientboundSetDisplayObjectivePacket(DISPLAY_SLOT_SIDEBAR, objective))
    }

    private fun sendScorePacket(index: Int, method: Method, owner: String) {
        this.owner.connection.send(ClientboundSetScorePacket(method, this.objective.name, owner, index))
    }

    companion object {
        const val MAX_SIZE = 14

        private val DUMMY = Scoreboard()

        private fun createEmptyObjective(): Objective {
            return Objective(
                DUMMY,
                Random.nextInt(Int.MAX_VALUE).toString(),
                ObjectiveCriteria.DUMMY,
                Component.empty(),
                ObjectiveCriteria.RenderType.INTEGER
            )
        }
    }
}