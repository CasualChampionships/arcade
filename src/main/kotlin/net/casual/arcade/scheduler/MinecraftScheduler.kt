package net.casual.arcade.scheduler

import net.casual.arcade.task.Task

/**
 * This interface provides methods for scheduling [Task]s
 * in the future on the main server thread.
 *
 * @see TickedScheduler
 * @see GlobalTickedScheduler
 */
public interface MinecraftScheduler {
    /**
     * This method will schedule a [task] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [task].
     * @param task The runnable to be scheduled.
     */
    public fun schedule(duration: MinecraftTimeDuration, task: Task)


    /**
     * This schedules a [task] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [task] for a given [duration].
     *
     * @param delay The initial delay before the first [task] is scheduled.
     * @param interval The amount of time between each [task].
     * @param duration The total duration the loop should be running for.
     * @param task The runnable to be scheduled.
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
}