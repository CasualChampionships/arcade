package net.casual.arcade.minigame.managers

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.utils.time.MinecraftTimeUnit

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

    /**
     * This returns the phased scheduler.
     *
     * @return The phased scheduler.
     */
    public fun asPhasedScheduler(): MinecraftScheduler {
        return this.phased
    }

    internal fun tick() {
        this.minigame.tick()
        this.phased.tick()
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [task].
     * @param task The runnable to be scheduled.
     */
    override fun schedule(duration: MinecraftTimeDuration, task: Task) {
        this.minigame.schedule(duration, task)
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [duration].
     *
     * If the minigame's phase changes before the [duration]
     * the task will no longer run.
     *
     * @param duration The duration to wait before running the [task].
     * @param task The runnable to be scheduled.
     */
    public fun schedulePhased(duration: MinecraftTimeDuration, task: Task) {
        this.phased.schedule(duration, task)
    }

    /**
     * This method will schedule a task which will be made cancellable.
     * The user can cancel the task, *or* the minigame may cancel the event
     * in the case that the phase changes.
     *
     * @param duration The duration to wait before running the [task].
     * @param task The runnable to be scheduled.
     * @return The cancellable task.
     */
    public fun schedulePhasedCancellable(duration: MinecraftTimeDuration, task: Task): CancellableTask {
        val cancellable = CancellableTask.of(task)
        this.schedulePhased(duration, cancellable)
        return cancellable
    }

    /**
     * This method will schedule a task which will be made cancellable.
     * The user can cancel the task, *or* the minigame may cancel the event
     * in the case that the phase changes.
     *
     * @param time The amount of time to wait before running the [task].
     * @param unit The units of time.
     * @param task The runnable to be scheduled.
     * @return The cancellable task.
     */
    public fun schedulePhasedCancellable(time: Int, unit: MinecraftTimeUnit, task: Task): CancellableTask {
        return this.schedulePhasedCancellable(unit.duration(time), task)
    }

    /**
     * This schedules a [task] in a loop with a given
     * initial [delay] and with a given [interval] between
     * each invocation of the [task] for a given [duration].
     *
     * If the minigame's phase changes, some of the scheduled
     * tasks will not be run.
     *
     * @param delay The initial delay before the first [task] is scheduled.
     * @param interval The amount of time between each [task].
     * @param duration The total duration the loop should be running for.
     * @param task The runnable to be scheduled.
     */
    public fun schedulePhasedInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        task: Task
    ) {
        this.phased.scheduleInLoop(delay, interval, duration, task)
    }
}