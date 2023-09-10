package net.casual.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.task.Task
import java.util.*
import java.util.function.IntFunction

/**
 * This class is an implementation of [MinecraftScheduler] which
 * allows you to schedule [Runnable]s for a later time on the
 * main server thread.
 *
 * @see MinecraftScheduler
 * @see GlobalTickedScheduler
 */
class TickedScheduler: MinecraftScheduler {
    internal val tasks: Int2ObjectMap<Queue<Runnable>> = Int2ObjectOpenHashMap()
    internal var tickCount = 0

    /**
     * This advances the scheduler by one tick.
     *
     * All [Runnable]s that were scheduled for this
     * tick will be run then removed.
     */
    fun tick() {
        val queue = this.tasks.remove(this.tickCount++)
        if (queue !== null) {
            queue.forEach(Runnable::run)
            queue.clear()
        }
    }

    /**
     * This method will schedule a [runnable] to be run
     * after a given [duration].
     *
     * @param duration The duration to wait before running the [runnable].
     * @param runnable The runnable to be scheduled.
     */
    override fun schedule(duration: MinecraftTimeDuration, runnable: Runnable) {
        this.tasks.computeIfAbsent(this.tickCount + duration.toTicks(), IntFunction { ArrayDeque() }).add(runnable)
    }
}