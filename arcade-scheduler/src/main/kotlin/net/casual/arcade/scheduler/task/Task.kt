/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task

import net.casual.arcade.scheduler.TickedScheduler

/**
 * This interface represents a [Task] used in the
 * [TickedScheduler] which can be run.
 *
 * @see TickedScheduler
 * @see ScheduledTask
 */
public fun interface Task {
    /**
     * This runs the task.
     */
    public fun run()
}