package net.casual.arcade.gui

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

/**
 * This is the base class for every server-side
 * UI component that supports multiple players.
 *
 * You can set the component update interval
 * with the [setInterval] method.
 * And add, remove, or clear players from the UI
 * using [addPlayer], [removePlayer], and [clearPlayers]
 * respectively.
 */
abstract class PlayerUI {
    private val connections = HashSet<ServerGamePacketListenerImpl>()

    internal var interval = 1
        private set

    protected abstract fun onAddPlayer(player: ServerPlayer)

    protected abstract fun onRemovePlayer(player: ServerPlayer)

    /**
     * Sets the interval at which components in the UI
     * are updated (in ticks).
     *
     * @param interval The duration between each update; cannot be less than 1.
     */
    fun setInterval(interval: Int) {
        this.interval = interval.coerceAtLeast(1)
    }

    /**
     * Adds a player to the [PlayerUI] component.
     * They will then be displayed the [PlayerUI] component.
     *
     * @param player The player to add.
     */
    fun addPlayer(player: ServerPlayer) {
        if (this.connections.add(player.connection)) {
            this.onAddPlayer(player)
        }
    }

    /**
     * Removes a player from the [PlayerUI] component.
     * They will no longer be displayed the [PlayerUI] component.
     *
     * @param player The player to remove.
     */
    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            this.onRemovePlayer(player)
        }
    }

    /**
     * Clears all the players from the [PlayerUI] component.
     */
    fun clearPlayers() {
        for (player in this.getPlayers()) {
            this.removePlayer(player)
        }
    }

    /**
     * Gets all the players that are being displayed
     * the [PlayerUI] component.
     *
     * @return All the players being displayed the [PlayerUI].
     */
    fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }
}