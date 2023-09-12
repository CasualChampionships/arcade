package net.casual.arcade.minigame

import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.task.SavableTask

/**
 * This is an implementation of [MinigameScheduler] that allows for
 * scheduling for a minigame as well as each phase of the minigame.
 *
 * If you schedule a task and the minigame ends, then the task will
 * no longer be run.
 * Similarly, if you schedule a phased task and the minigame changes
 * phase, the task will no longer be run.
 *
 * All [SavableTask]s that are scheduled, either to the minigame or
 * to a minigame phase, will be saved if your minigame implementation
 * is a [SavableMinigame].
 *
 * @see MinecraftScheduler
 * @see Minigame
 */
public class MinigameScheduler internal constructor(): MinecraftScheduler {
    internal val minigame = TickedScheduler()
    internal val phased = TickedScheduler()

    internal fun tick() {
        this.minigame.tick()
        this.phased.tick()
    }

    /**
     * This method will schedule a [runnable] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [runnable].
     * @param runnable The runnable to be scheduled.
     */
    override fun schedule(duration: MinecraftTimeDuration, runnable: Runnable) {
        this.minigame.schedule(duration, runnable)
    }

    /**
     * This method will schedule a [runnable] to be run
     * after a given [duration].
     *
     * If the minigame's phase changes before the [duration]
     * the task will no longer run.
     *
     * @param duration The duration to wait before running the [runnable].
     * @param runnable The runnable to be scheduled.
     */
    public fun schedulePhased(duration: MinecraftTimeDuration, runnable: Runnable) {
        this.phased.schedule(duration, runnable)
    }

    /**
     * This method will schedule a [runnable] to be run
     * after a given [time] with units [unit].
     *
     * If the minigame's phase changes before the [time]
     * the task will no longer run.
     *
     * @param time The amount of time to wait before running the [runnable].
     * @param unit The units of time, by default [Ticks].
     * @param runnable The runnable to be scheduled.
     */
    public fun schedulePhased(time: Int, unit: MinecraftTimeUnit, runnable: Runnable) {
        this.phased.schedule(time, unit, runnable)
    }

    /**
     * This schedules a [runnable] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [runnable] for a given [duration].
     *
     * If the minigame's phase changes, some of the scheduled
     * tasks will not be run.
     *
     * @param delay The initial delay before the first [runnable] is scheduled.
     * @param interval The amount of time between each [runnable].
     * @param duration The total duration the loop should be running for.
     * @param runnable The runnable to be scheduled.
     */
    public fun schedulePhasedInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        runnable: Runnable
    ) {
        this.phased.scheduleInLoop(delay, interval, duration, runnable)
    }

    /**
     * This schedules a [runnable] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [runnable] for a given [duration].
     *
     * If the minigame's phase changes, some of the scheduled
     * tasks will not be run.
     *
     * @param delay The initial delay before the first [runnable] is scheduled.
     * @param interval The amount of time between each [runnable].
     * @param duration The total duration the loop should be running for.
     * @param unit The units of time for [delay], [interval], and [duration].
     * @param runnable The runnable to be scheduled.
     */
    public fun schedulePhasedInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        runnable: Runnable
    ) {
        this.phased.scheduleInLoop(delay, interval, duration, unit, runnable)
    }
}