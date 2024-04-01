package net.casual.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.task.Task
import net.casual.arcade.task.impl.CancellableTask
import java.util.*
import java.util.function.IntFunction

/**
 * This class is an implementation of [MinecraftScheduler] which
 * allows you to schedule [Task]s for a later time on the
 * main server thread.
 *
 * @see MinecraftScheduler
 * @see GlobalTickedScheduler
 */
public class TickedScheduler: MinecraftScheduler {
    internal val tasks: Int2ObjectMap<Queue<Task>> = Int2ObjectOpenHashMap()
    internal var tickCount = 0

    /**
     * This advances the scheduler by one tick.
     *
     * All [Task]s that were scheduled for this
     * tick will be run then removed.
     */
    public fun tick() {
        val queue = this.tasks.remove(this.tickCount++)
        if (queue !== null) {
            queue.forEach(Runnable::run)
            queue.clear()
        }
    }

    /**
     * This cancels and removes all tasks with a
     * given tick delta.
     *
     * @param delta The tick delta.
     */
    public fun cancel(delta: Int = 0) {
        val queue = this.tasks.remove(this.tickCount + delta)
        for (task in queue) {
            if (task is CancellableTask) {
                task.cancel()
            }
        }
    }

    /**
     * This cancels all the tasks that are currently
     * scheduled in the scheduler.
     */
    public fun cancelAll() {
        for (ticked in this.tasks.values) {
            for (task in ticked) {
                if (task is CancellableTask) {
                    task.cancel()
                }
            }
        }
        this.tasks.clear()
    }

    /**
     * This method will schedule a [task] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [ta  ].
     * @param task The task to be scheduled.
     */
    override fun schedule(duration: MinecraftTimeDuration, task: Task) {
        this.tasks.computeIfAbsent(this.tickCount + duration.ticks, IntFunction { ArrayDeque() }).add(task)
    }
}