package net.casual.arcade.visuals.sidebar

import net.casual.arcade.visuals.core.PlayerUI
import net.casual.arcade.visuals.core.TickableUI
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.extensions.PlayerSidebarExtension.Companion.sidebar
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

public class Sidebar(title: PlayerSpecificElement<Component>): PlayerUI(), TickableUI {
    private val rows = ArrayList<PlayerSpecificElement<SidebarComponent>>(MAX_SIZE)

    public var title: PlayerSpecificElement<Component> = title
        private set

    public fun size(): Int {
        return this.rows.size
    }

    public fun setTitle(title: PlayerSpecificElement<Component>) {
        this.title = title

        for (player in this.getPlayers()) {
            player.sidebar.setTitle(title.get(player))
        }
    }

    public fun getRow(index: Int): PlayerSpecificElement<SidebarComponent> {
        this.checkBounds(index, this.size() - 1)
        return this.rows[index]
    }

    public fun addRow(row: PlayerSpecificElement<SidebarComponent>) {
        // Add to the bottom
        this.addRow(0, row)
    }

    public fun addRow(index: Int, row: PlayerSpecificElement<SidebarComponent>) {
        require(this.size() < MAX_SIZE) { "Cannot add more rows, already at max size: $MAX_SIZE" }
        this.checkBounds(index, this.size())

        this.rows.add(index, row)

        for (player in this.getPlayers()) {
            player.sidebar.addRow(index, row.get(player))
        }
    }

    public fun setRow(index: Int, row: PlayerSpecificElement<SidebarComponent>) {
        this.checkBounds(index, this.size() - 1)

        this.rows[index] = row

        for (player in this.getPlayers()) {
            player.sidebar.setRow(index, row.get(player))
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

    override fun tick(server: MinecraftServer) {
        for (row in this.rows) {
            row.tick(server)
        }
    }

    private fun checkBounds(index: Int, upper: Int) {
        require(index in 0..upper) { "Row index $index out of bounds! Must between 0 and $upper" }
    }

    private companion object {
        const val MAX_SIZE = 14
    }
}