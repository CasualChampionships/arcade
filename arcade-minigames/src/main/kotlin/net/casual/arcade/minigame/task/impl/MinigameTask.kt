/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.capture.CaptureConsumerTask
import net.casual.arcade.scheduler.task.capture.CaptureSerializer
import net.casual.arcade.scheduler.task.capture.CaptureTask

@Suppress("FunctionName", "UNCHECKED_CAST")
public fun <M: Minigame> MinigameTask(minigame: M, task: CaptureConsumerTask<M>): Task {
    return CaptureTask(minigame.uuid, { Minigames.get(it) as M }, CaptureSerializer.same(), task)
}
