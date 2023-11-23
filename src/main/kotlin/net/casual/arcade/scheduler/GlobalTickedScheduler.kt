package net.casual.arcade.scheduler

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casual.arcade.task.Task

/**
 * This is a global implementation of a [TickedScheduler], you
 * can simply schedule any [Runnable]s here, and they will be
 * run later.
 *
 * However, it is advised that you use your own [MinecraftScheduler]
 * as it allows you more flexibility.
 * For example, if you are scheduling tasks for a minigame, it
 * is beneficial to use the [Minigame.scheduler], see [MinigameScheduler]
 * for more information.
 *
 * @see MinecraftScheduler
 * @see TickedScheduler
 * @see MinigameScheduler
 */
public object GlobalTickedScheduler {
    private val scheduler = TickedScheduler()

    init {
        GlobalEventHandler.register<ServerTickEvent> { this.scheduler.tick() }
    }

    /**
     * This method will schedule a [task] to be run later in
     * the tick.
     * This is useful if you need to execute something after it
     * has been initialized.
     *
     * @param task The runnable to be scheduled.
     */
    @JvmStatic
    public fun later(task: Task) {
        this.schedule(MinecraftTimeDuration.ZERO, task)
    }

    /**
     * This method will schedule a [runnable] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [runnable].
     * @param runnable The runnable to be scheduled.
     */
    @JvmStatic
    public fun schedule(duration: MinecraftTimeDuration, runnable: Task) {
        this.scheduler.schedule(duration, runnable)
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [time] with units [unit].
     *
     * @param time The amount of time to wait before running the [task].
     * @param unit The units of time, by default [Ticks].
     * @param task The runnable to be scheduled.
     */
    @JvmStatic
    public fun schedule(time: Int, unit: MinecraftTimeUnit, task: Task) {
        this.scheduler.schedule(time, unit, task)
    }

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
    @JvmStatic
    public fun scheduleInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        task: Task
    ) {
        this.scheduler.scheduleInLoop(delay, interval, duration, task)
    }

    /**
     * This schedules a [task] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [task] for a given [duration].
     *
     * @param delay The initial delay before the first [task] is scheduled.
     * @param interval The amount of time between each [task].
     * @param duration The total duration the loop should be running for.
     * @param unit The units of time for [delay], [interval], and [duration].
     * @param task The runnable to be scheduled.
     */
    @JvmStatic
    public fun scheduleInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        task: Task
    ) {
        this.scheduler.scheduleInLoop(delay, interval, duration, unit, task)
    }
}