/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import kotlinx.coroutines.CoroutineScope
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.utils.schedule
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.utils.side.LogicalSide

/**
 * This is an implementation of [MinigameTickedScheduler] that allows for
 * scheduling for a minigame as well as each phase of the minigame.
 *
 * If you schedule a task and the minigame ends, then the task will
 * no longer be run.
 * Similarly, if you schedule a phased task and the minigame changes
 * phase, the task will no longer be run.
 *
 * All [net.casual.arcade.scheduler.task.routine.Routine]s that are scheduled,
 * either to the minigame or to a minigame phase, will be saved if your
 * minigame is serializable. Plain tasks are transient and are not saved.
 *
 * @see TickedScheduler
 * @see Minigame
 */
public class MinigameTickedScheduler(
    private val minigame: Minigame
): TickedScheduler {
    internal val standard = SimpleTickedScheduler.server()
    internal val phased = SimpleTickedScheduler.server()

    override val target: LogicalSide
        get() = LogicalSide.Server

    /**
     * This returns the phased scheduler.
     *
     * @return The phased scheduler.
     */
    public fun asPhasedScheduler(): TickedScheduler {
        return this.phased
    }

    internal fun tick() {
        this.standard.tick()
        this.phased.tick()
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [delay].
     *
     * @param delay The duration to wait before running the [task].
     * @param task The runnable to be scheduled.
     * @return A handle which can be used to cancel the task.
     */
    override fun schedule(delay: MinecraftTimeDuration, task: Task): ScheduledTask {
        return this.standard.schedule(delay, task)
    }

    override fun asCoroutineScope(): CoroutineScope {
        return this.standard.asCoroutineScope()
    }

    /**
     * This method will schedule a [routine] to be started after a given [delay],
     * running against this scheduler's minigame.
     *
     * @param delay The duration to wait before starting the [routine].
     * @param routine The routine to schedule, which must be registered.
     * @return A handle which can be used to cancel the routine.
     */
    public fun <M: Minigame> schedule(delay: MinecraftTimeDuration, routine: Routine<M>): ScheduledTask {
        @Suppress("UNCHECKED_CAST")
        return this.standard.schedule(delay, routine, this.minigame as M)
    }

    /**
     * This method will schedule a [routine] to be started after a given [delay].
     *
     * If the minigame's phase changes before the routine finishes, it will be
     * cancelled, unwinding through any `finally` blocks it has.
     *
     * @param delay The duration to wait before starting the [routine].
     * @param routine The routine to schedule, which must be registered.
     * @return A handle which can be used to cancel the routine.
     */
    public fun <M: Minigame> schedulePhased(delay: MinecraftTimeDuration, routine: Routine<M>): ScheduledTask {
        @Suppress("UNCHECKED_CAST")
        return this.phased.schedule(delay, routine, this.minigame as M)
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
     * @return A handle which can be used to cancel the task.
     */
    public fun schedulePhased(duration: MinecraftTimeDuration, task: Task): ScheduledTask {
        return this.phased.schedule(duration, task)
    }
}