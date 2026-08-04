/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task

import net.casual.arcade.scheduler.TickedScheduler

/**
 * A handle to a scheduled task, allowing for cancelling said task.
 *
 * These handles are minimal, if you want to await the task
 * you should utilize coroutines instead using [TickedScheduler.asCoroutineScope].
 *
 * @see TickedScheduler
 */
public interface ScheduledTask {
    /**
     * Whether this has already run, or been cancelled.
     */
    public val isFinished: Boolean

    /**
     * Cancels this task, preventing it from running.
     */
    public fun cancel()
}
