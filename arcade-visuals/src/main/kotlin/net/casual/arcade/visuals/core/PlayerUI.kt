/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.core

import net.minecraft.server.level.ServerPlayer

public interface PlayerUI {
    /**
     * Adds a player to the [PlayerUI] component.
     * They will then be displayed the [PlayerUI] component.
     *
     * @param player The player to add.
     */
    public fun addPlayer(player: ServerPlayer)

    /**
     * Removes a player from the [PlayerUI] component.
     * They will no longer be displayed the [PlayerUI] component.
     *
     * @param player The player to remove.
     */
    public fun removePlayer(player: ServerPlayer)

    /**
     * Clears all the players from the [PlayerUI] component.
     */
    public fun clearPlayers()
}