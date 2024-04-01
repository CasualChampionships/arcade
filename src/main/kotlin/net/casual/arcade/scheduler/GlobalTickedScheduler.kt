package net.casual.arcade.scheduler

import net.casual.arcade.events.BuiltInEventPhases.POST
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameScheduler
import net.casual.arcade.task.Task
import net.casual.arcade.utils.TimeUtils.Ticks

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
    private val schedulers = ArrayList<TickedScheduler>()
    private val scheduler = TickedScheduler()

    init {
        GlobalEventHandler.register<ServerTickEvent>(phase = POST) {
            this.scheduler.tick()
            for (scheduler in this.schedulers) {
                scheduler.tick()
            }
        }
    }

    public fun asScheduler(): MinecraftScheduler {
        return this.scheduler
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

    @JvmStatic
    public fun temporaryScheduler(
        lifetime: MinecraftTimeDuration
    ): TickedScheduler {
        val temporary = TickedScheduler()
        this.schedulers.add(temporary)
        this.schedule(lifetime + 1.Ticks) {
            this.schedulers.remove(temporary)
        }
        return temporary
    }
}