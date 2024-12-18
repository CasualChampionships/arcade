/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a new player
 * joins the minigame.
 */
public data class MinigameAddNewPlayerEvent(
    override val minigame: Minigame,
    val player: ServerPlayer,
    var spectating: Boolean?,
    var admin: Boolean?
): CancellableEvent.Default(), MinigameEvent