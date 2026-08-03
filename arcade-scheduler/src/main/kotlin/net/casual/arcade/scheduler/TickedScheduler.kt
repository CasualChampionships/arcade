/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import kotlinx.coroutines.CoroutineScope
import net.casual.arcade.scheduler.TickedScheduler.Companion.schedule
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.side.LogicalSide
import net.casual.arcade.utils.time.MinecraftTimeDuration

/**
 * This interface provides methods for scheduling [Task]s
 * in the future on the main thread of a given [LogicalSide].
 *
 * @see SimpleTickedScheduler
 * @see GlobalTickedScheduler
 */
public interface TickedScheduler {
    /**
     * The logical side this scheduler is ticked on.
     *
     * This must match the side you tick the scheduler from, otherwise
     * any coroutines dispatched onto it will resume on the wrong thread.
     */
    public val target: LogicalSide

    /**
     * This method will schedule a [task] to be run
     * after a given [delay].
     *
     * [Task]s are one-shot and transient. If you need control flow, for
     * example a loop or waiting on something, launch a coroutine with
     * [asCoroutineScope] instead. If you need work which survives a restart
     * use a [Routine].
     *
     * @param delay The duration to wait before running the [task].
     * @param task The task to be scheduled.
     * @return A handle which can be used to cancel the task.
     */
    public fun schedule(delay: MinecraftTimeDuration, task: Task): ScheduledTask

    /**
     * A [CoroutineScope] which dispatches onto this scheduler.
     *
     * Coroutines launched here resume on the main thread of [target], and
     * [net.casual.arcade.utils.coroutine.delay] measures its duration in this
     * scheduler's ticks. This is the preferred way to write anything which
     * isn't a single one-shot [Task]:
     * ```
     * scheduler.asCoroutineScope().launch {
     *     while (isActive) {
     *         doSomething()
     *         delay(5.Ticks)
     *     }
     * }
     * ```
     *
     * The returned scope is the same for the lifetime of this scheduler, and its
     * children are cancelled whenever the scheduler cancels everything. The scope
     * itself stays usable afterwards, so a scheduler may be reused.
     *
     * Coroutines are transient; nothing launched here survives a restart.
     *
     * @return The scope which dispatches onto this scheduler.
     */
    public fun asCoroutineScope(): CoroutineScope

    public companion object {
        /**
         * This method allows Java callers to call the [schedule] method
         * as the [MinecraftTimeDuration] value class is not available.
         *
         * @param ticks The number of ticks to schedule the task for.
         * @param task The task to schedule.
         * @return A handle which can be used to cancel the task.
         */
        @JvmStatic
        public fun TickedScheduler.schedule(ticks: Int, task: Task): ScheduledTask {
            return this.schedule(ticks.Ticks, task)
        }
    }
}
