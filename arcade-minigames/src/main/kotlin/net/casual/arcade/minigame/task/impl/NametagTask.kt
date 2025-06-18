/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.visuals.nametag.PlayerNametag

public open class NametagTask(
    private val minigame: Minigame,
    private val nametag: PlayerNametag
): Task {
    init {
        this.minigame.ui.addNametag(this.nametag)
    }

    final override fun run() {
        this.minigame.ui.removeNametag(this.nametag)
    }
}