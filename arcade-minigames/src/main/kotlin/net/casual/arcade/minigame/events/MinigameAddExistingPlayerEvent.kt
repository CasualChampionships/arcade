/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a player who was
 * previously part of a minigame is re-added.
 *
 * For example: when the player relogs during a minigame.
 */
public data class MinigameAddExistingPlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer,
    var spectating: Boolean?,
    var admin: Boolean?
): MinigameEvent