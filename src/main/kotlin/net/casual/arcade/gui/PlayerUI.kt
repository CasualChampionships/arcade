package net.casual.arcade.gui

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.jetbrains.annotations.ApiStatus.OverrideOnly

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
public abstract class PlayerUI {
    private val connections = HashSet<ServerGamePacketListenerImpl>()

    internal var interval = 1
        private set

    @OverrideOnly
    protected abstract fun onAddPlayer(player: ServerPlayer)

    @OverrideOnly
    protected abstract fun onRemovePlayer(player: ServerPlayer)

    /**
     * Sets the interval at which components in the UI
     * are updated (in ticks).
     *
     * @param interval The duration between each update; cannot be less than 1.
     */
    public fun setInterval(interval: Int) {
        this.interval = interval.coerceAtLeast(1)
    }

    /**
     * Adds a player to the [PlayerUI] component.
     * They will then be displayed the [PlayerUI] component.
     *
     * @param player The player to add.
     */
    public fun addPlayer(player: ServerPlayer) {
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
    public fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            this.onRemovePlayer(player)
        }
    }

    /**
     * Clears all the players from the [PlayerUI] component.
     */
    public fun clearPlayers() {
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
    public fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }
}