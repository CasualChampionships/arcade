package net.casualuhc.arcade.scoreboards

import net.casualuhc.arcade.utils.SidebarUtils
import net.casualuhc.arcade.utils.SidebarUtils.sidebar
import net.minecraft.server.level.ServerPlayer

class ArcadeSidebar(title: SidebarRow) {
    private val players = HashSet<ServerPlayer>()
    private val rows = ArrayList<SidebarRow>(SidebarUtils.MAX_SIZE)

    var title: SidebarRow = title
        private set

    var interval = 1
        private set

    fun size(): Int {
        return this.rows.size
    }

    fun setTitle(title: SidebarRow) {
        this.title = title

        for (player in this.players) {
            player.sidebar.setTitle(title.getComponent(player))
        }
    }

    fun setInterval(interval: Int) {
        this.interval = interval.coerceAtLeast(1)
    }

    fun getRow(index: Int): SidebarRow {
        this.checkBounds(index, this.size() - 1)
        return this.rows[index]
    }

    fun addRow(row: SidebarRow) {
        this.addRow(this.size(), row)
    }

    fun addRow(index: Int, row: SidebarRow) {
        require(this.size() < SidebarUtils.MAX_SIZE) { "Cannot add more rows, already at max size: ${SidebarUtils.MAX_SIZE}" }
        this.checkBounds(index, this.size())

        this.rows.add(index, row)

        for (player in this.players) {
            player.sidebar.addRow(index, row.getComponent(player))
        }
    }

    fun setRow(index: Int, row: SidebarRow) {
        this.checkBounds(index, this.size() - 1)

        this.rows[index] = row

        for (player in this.players) {
            player.sidebar.setRow(index, row.getComponent(player))
        }
    }

    fun removeRow(index: Int) {
        this.checkBounds(index, this.size() - 1)

        this.rows.removeAt(index)

        for (player in this.players) {
            player.sidebar.removeRow(index)
        }
    }

    fun addPlayer(player: ServerPlayer) {
        if (this.players.add(player)) {
            player.sidebar.set(this)
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.players.remove(player)) {
            player.sidebar.remove()
        }
    }

    private fun checkBounds(index: Int, upper: Int) {
        require(index in 0..upper) { "Row index $index out of bounds! Must between 0 and $upper" }
    }
}