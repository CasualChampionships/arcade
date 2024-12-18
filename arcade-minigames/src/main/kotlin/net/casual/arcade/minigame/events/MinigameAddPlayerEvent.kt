/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a player, either
 * new or existing is added to a minigame.
 */
public data class MinigameAddPlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer,
    var spectating: Boolean?,
    var admin: Boolean?
): MinigameEvent