package net.casual.arcade.gui.sidebar

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.SidebarUtils
import net.casual.arcade.utils.SidebarUtils.sidebar
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class ArcadeSidebar(title: ComponentSupplier): PlayerUI() {
    private val rows = ArrayList<ComponentSupplier>(SidebarUtils.MAX_SIZE)

    var title: ComponentSupplier = title
        private set

    fun size(): Int {
        return this.rows.size
    }

    fun setTitle(title: ComponentSupplier) {
        this.title = title

        for (player in this.getPlayers()) {
            player.sidebar.setTitle(title.getComponent(player))
        }
    }

    fun getRow(index: Int): ComponentSupplier {
        this.checkBounds(index, this.size() - 1)
        return this.rows[index]
    }

    fun addRow(row: ComponentSupplier) {
        // Add to the bottom
        this.addRow(0, row)
    }

    fun addRow(index: Int, row: ComponentSupplier) {
        require(this.size() < SidebarUtils.MAX_SIZE) { "Cannot add more rows, already at max size: ${SidebarUtils.MAX_SIZE}" }
        this.checkBounds(index, this.size())

        this.rows.add(index, row)

        for (player in this.getPlayers()) {
            player.sidebar.addRow(index, row.getComponent(player))
        }
    }

    fun setRow(index: Int, row: ComponentSupplier) {
        this.checkBounds(index, this.size() - 1)

        this.rows[index] = row

        for (player in this.getPlayers()) {
            player.sidebar.setRow(index, row.getComponent(player))
        }
    }

    fun removeRow(index: Int) {
        this.checkBounds(index, this.size() - 1)

        this.rows.removeAt(index)

        for (player in this.getPlayers()) {
            player.sidebar.removeRow(index)
        }
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.sidebar.set(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.sidebar.remove()
    }

    private fun checkBounds(index: Int, upper: Int) {
        require(index in 0..upper) { "Row index $index out of bounds! Must between 0 and $upper" }
    }
}