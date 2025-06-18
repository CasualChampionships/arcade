/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.core

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.jetbrains.annotations.ApiStatus.Internal
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.util.function.Consumer

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
public abstract class TrackedPlayerUI: PlayerUI {
    private val connections = ReferenceOpenHashSet<ServerGamePacketListenerImpl>()

    /**
     * The interval at which the components are updated (in ticks).
     */
    public var interval: Int = 1
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
     * Adds a player to the [TrackedPlayerUI] component.
     * They will then be displayed the [TrackedPlayerUI] component.
     *
     * @param player The player to add.
     */
    public override fun addPlayer(player: ServerPlayer) {
        if (this.connections.add(player.connection)) {
            this.onAddPlayer(player)
        }
    }

    /**
     * Removes a player from the [TrackedPlayerUI] component.
     * They will no longer be displayed the [TrackedPlayerUI] component.
     *
     * @param player The player to remove.
     */
    public override fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            this.onRemovePlayer(player)
        }
    }

    /**
     * Clears all the players from the [TrackedPlayerUI] component.
     */
    public override fun clearPlayers() {
        for (player in this.getPlayers()) {
            this.removePlayer(player)
        }
    }

    /**
     * Whether this player is viewing this ui element.
     *
     * @return Whether the player is viewing the ui element.
     */
    public fun hasPlayer(player: ServerPlayer): Boolean {
        return this.connections.contains(player.connection)
    }

    /**
     * Gets all the players that are being displayed
     * the [TrackedPlayerUI] component.
     *
     * @return All the players being displayed the [TrackedPlayerUI].
     */
    public fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }

    /**
     * This sends a given packet to all watching players.
     *
     * @param packet The packet to send.
     */
    public fun sendToAllPlayers(packet: Packet<*>) {
        for (connection in this.connections) {
            connection.send(packet)
        }
    }

    @Internal
    public fun resendToPlayer(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        if (this.connections.contains(player.connection)) {
            this.resendTo(player, sender)
        }
    }

    protected abstract fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>)
}