package net.casual.arcade.scheduler

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks

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
     * This method will schedule a [runnable] to be run later in
     * the tick.
     * This is useful if you need to execute something after it
     * has been initialized.
     *
     * @param runnable The runnable to be scheduled.
     */
    @JvmStatic
    public fun later(runnable: Runnable) {
        this.schedule(MinecraftTimeDuration.ZERO, runnable)
    }

    /**
     * This method will schedule a [runnable] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [runnable].
     * @param runnable The runnable to be scheduled.
     */
    @JvmStatic
    public fun schedule(duration: MinecraftTimeDuration, runnable: Runnable) {
        this.scheduler.schedule(duration, runnable)
    }

    /**
     * This method will schedule a [runnable] to be run
     * after a given [time] with units [unit].
     *
     * @param time The amount of time to wait before running the [runnable].
     * @param unit The units of time, by default [Ticks].
     * @param runnable The runnable to be scheduled.
     */
    @JvmStatic
    public fun schedule(time: Int, unit: MinecraftTimeUnit, runnable: Runnable) {
        this.scheduler.schedule(time, unit, runnable)
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
    @JvmStatic
    public fun scheduleInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        runnable: Runnable
    ) {
        this.scheduler.scheduleInLoop(delay, interval, duration, runnable)
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
    @JvmStatic
    public fun scheduleInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        runnable: Runnable
    ) {
        this.scheduler.scheduleInLoop(delay, interval, duration, unit, runnable)
    }
}