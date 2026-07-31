/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task

import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.impl.CancellableTask

/**
 * This interface represents a [Task] used in the
 * [TickedScheduler] which can be run.
 *
 * Tasks are transient; they are never serialized. Use a
 * [net.casual.arcade.scheduler.task.routine.Routine] for work which must
 * survive a restart. Tasks may be cancellable, see [CancellableTask].
 *
 * @see TickedScheduler
 * @see CancellableTask
 */
public fun interface Task: Runnable {
    /**
     * This runs the task.
     */
    override fun run()
}