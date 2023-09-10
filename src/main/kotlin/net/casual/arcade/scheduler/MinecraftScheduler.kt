package net.casual.arcade.scheduler

import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks

/**
 * This interface provides methods for scheduling [Runnable]s
 * in the future on the main server thread.
 *
 * @see TickedScheduler
 * @see GlobalTickedScheduler
 */
interface MinecraftScheduler {
    /**
     * This method will schedule a [runnable] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [runnable].
     * @param runnable The runnable to be scheduled.
     */
    fun schedule(duration: MinecraftTimeDuration, runnable: Runnable)

    /**
     * This method will schedule a [runnable] to be run
     * after a given [time] with units [unit].
     *
     * @param time The amount of time to wait before running the [runnable].
     * @param unit The units of time, by default [Ticks].
     * @param runnable The runnable to be scheduled.
     */
    fun schedule(time: Int, unit: MinecraftTimeUnit = Ticks, runnable: Runnable) {
        this.schedule(unit.duration(time), runnable)
    }

    /**
     * This schedules a [runnable] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [runnable] for a given [duration].
     *
     * @param delay The initial delay before the first [runnable] is scheduled.
     * @param interval The amount of time between each [runnable].
     * @param duration The total duration the loop should be running for.
     * @param runnable The runnable to be scheduled.
     */
    fun scheduleInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        runnable: Runnable
    ) {
        val total = duration + delay
        var current = delay
        while (current < total) {
            this.schedule(current, runnable)
            current += interval
        }
    }

    /**
     * This schedules a [runnable] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [runnable] for a given [duration].
     *
     * @param delay The initial delay before the first [runnable] is scheduled.
     * @param interval The amount of time between each [runnable].
     * @param duration The total duration the loop should be running for.
     * @param unit The units of time for [delay], [interval], and [duration].
     * @param runnable The runnable to be scheduled.
     */
    fun scheduleInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        runnable: Runnable
    ) {
        return this.scheduleInLoop(
            unit.duration(delay),
            unit.duration(interval),
            unit.duration(duration),
            runnable
        )
    }
}