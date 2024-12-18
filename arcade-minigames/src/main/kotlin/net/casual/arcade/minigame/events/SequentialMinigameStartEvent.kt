/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.template.minigame.SequentialMinigames

public data class SequentialMinigameStartEvent(
    override val minigame: Minigame,
    val minigames: SequentialMinigames
): MinigameEvent