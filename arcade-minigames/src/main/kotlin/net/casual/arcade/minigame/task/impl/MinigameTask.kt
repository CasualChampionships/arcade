/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.scheduler.task.Task
import java.util.function.Consumer

@Suppress("FunctionName", "UNCHECKED_CAST")
public fun <M: Minigame> MinigameTask(minigame: M, task: Consumer<M>): Task {
    val uuid = minigame.uuid
    return Task {
        val resolved = Minigames.get(uuid) as M?
        if (resolved != null) {
            task.accept(resolved)
        }
    }
}
