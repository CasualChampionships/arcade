/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler

import net.casual.arcade.scheduler.TickedScheduler.Companion.schedule
import net.casual.arcade.scheduler.task.Task
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
     * @param delay The duration to wait before running the [task].
     * @param task The task to be scheduled.
     */
    public fun schedule(delay: MinecraftTimeDuration, task: Task)

    /**
     * This schedules a [task] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [task] for a given [duration].
     *
     * @param delay The initial delay before the first [task] is scheduled.
     * @param interval The amount of time between each [task].
     * @param duration The total duration the loop should be running for.
     * @param task The task to be scheduled.
     */
    public fun scheduleInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        task: Task
    ) {
        val total = duration + delay
        var current = delay
        while (current < total) {
            this.schedule(current, task)
            current += interval
        }
    }

    public companion object {
        /**
         * This method allows Java callers to call the [schedule] method
         * as the [MinecraftTimeDuration] value class is not available.
         *
         * @param ticks The number of ticks to schedule the task for.
         * @param task The task to schedule.
         */
        @JvmStatic
        public fun TickedScheduler.schedule(ticks: Int, task: Task) {
            this.schedule(ticks.Ticks, task)
        }
    }
}