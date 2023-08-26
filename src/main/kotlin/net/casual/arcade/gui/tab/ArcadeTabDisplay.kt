package net.casual.arcade.gui.tab

import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.TabUtils.tabDisplay
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class ArcadeTabDisplay(header: ComponentSupplier, footer: ComponentSupplier) {
    private val connections = HashSet<ServerGamePacketListenerImpl>()

    var header = header
        private set
    var footer = footer
        private set

    var interval = 1
        private set

    fun setDisplay(header: ComponentSupplier, footer: ComponentSupplier) {
        this.header = header
        this.footer = footer

        for (player in this.getPlayers()) {
            player.tabDisplay.setDisplay(header.getComponent(player), footer.getComponent(player))
        }
    }

    fun setInterval(interval: Int) {
        this.interval = interval.coerceAtLeast(1)
    }

    fun addPlayer(player: ServerPlayer) {
        if (this.connections.add(player.connection)) {
            player.tabDisplay.set(this)
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            player.tabDisplay.remove()
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