/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.visuals.tab.PlayerListDisplay

public open class PlayerListTask(
    private val minigame: Minigame,
    display: PlayerListDisplay
): Task {
    init {
        this.minigame.ui.setPlayerListDisplay(display)
    }

    override fun run() {
        this.minigame.ui.removePlayerListDisplay()
    }
}