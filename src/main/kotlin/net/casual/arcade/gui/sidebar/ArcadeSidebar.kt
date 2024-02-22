package net.casual.arcade.gui.sidebar

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.SidebarUtils
import net.casual.arcade.utils.SidebarUtils.sidebar
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

public class ArcadeSidebar(title: ComponentSupplier): PlayerUI() {
    private val rows = ArrayList<SidebarSupplier>(SidebarUtils.MAX_SIZE)

    public var title: ComponentSupplier = title
        private set

    public fun size(): Int {
        return this.rows.size
    }

    public fun setTitle(title: ComponentSupplier) {
        this.title = title

        for (player in this.getPlayers()) {
            player.sidebar.setTitle(title.getComponent(player))
        }
    }

    public fun getRow(index: Int): SidebarSupplier {
        this.checkBounds(index, this.size() - 1)
        return this.rows[index]
    }

    public fun addRow(row: SidebarSupplier) {
        // Add to the bottom
        this.addRow(0, row)
    }

    public fun addRow(index: Int, row: SidebarSupplier) {
        require(this.size() < SidebarUtils.MAX_SIZE) { "Cannot add more rows, already at max size: ${SidebarUtils.MAX_SIZE}" }
        this.checkBounds(index, this.size())

        this.rows.add(index, row)

        for (player in this.getPlayers()) {
            player.sidebar.addRow(index, row.getComponent(player))
        }
    }

    public fun setRow(index: Int, row: SidebarSupplier) {
        this.checkBounds(index, this.size() - 1)

        this.rows[index] = row

        for (player in this.getPlayers()) {
            player.sidebar.setRow(index, row.getComponent(player))
        }
    }

    public fun removeRow(index: Int) {
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

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        player.sidebar.resend(sender)
    }

    private fun checkBounds(index: Int, upper: Int) {
        require(index in 0..upper) { "Row index $index out of bounds! Must between 0 and $upper" }
    }
}