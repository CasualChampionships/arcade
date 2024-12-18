/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.minecraft.server.level.ServerPlayer

/**
 * This event is fired when a player is set as playing,
 * but also when the re-join a [Minigame] already as playing.
 */
public data class MinigameLoadPlayingEvent(
    override val minigame: Minigame,
    val player: ServerPlayer
): MinigameEvent