/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task

import net.casual.arcade.scheduler.MinecraftTaskScheduler
import net.casual.arcade.scheduler.task.impl.CancellableTask

/**
 * This interface represents a [Task] used in the
 * [MinecraftTaskScheduler] which can be run.
 *
 * Tasks can be serializable, see [SavableTask],
 * or cancellable see [CancellableTask].
 *
 * @see MinecraftTaskScheduler
 * @see SavableTask
 * @see CancellableTask
 */
public fun interface Task: Runnable {
    /**
     * This runs the task.
     */
    override fun run()
}