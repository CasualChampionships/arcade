package net.casual.arcade.gui

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

abstract class PlayerUI {
    private val connections = HashSet<ServerGamePacketListenerImpl>()

    internal var interval = 1
        private set

    abstract fun onAddPlayer(player: ServerPlayer)

    abstract fun onRemovePlayer(player: ServerPlayer)

    fun setInterval(interval: Int) {
        this.interval = interval.coerceAtLeast(1)
    }

    fun addPlayer(player: ServerPlayer) {
        if (this.connections.add(player.connection)) {
            this.onAddPlayer(player)
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            this.onRemovePlayer(player)
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