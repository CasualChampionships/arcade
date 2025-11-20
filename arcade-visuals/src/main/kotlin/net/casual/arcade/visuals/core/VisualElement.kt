/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.core

import net.minecraft.server.level.ServerPlayer

/**
 * Interface representing a visual component that
 * can be viewed by multiple players.
 */
public interface VisualElement {
    /**
     * Adds a player to the [VisualElement] component.
     * They will then be displayed the [VisualElement] component.
     *
     * @param player The player to add.
     */
    public fun addPlayer(player: ServerPlayer)

    /**
     * Removes a player from the [VisualElement] component.
     * They will no longer be displayed the [VisualElement] component.
     *
     * @param player The player to remove.
     */
    public fun removePlayer(player: ServerPlayer)

    /**
     * Clears all the players from the [VisualElement] component.
     */
    public fun clearPlayers()
}