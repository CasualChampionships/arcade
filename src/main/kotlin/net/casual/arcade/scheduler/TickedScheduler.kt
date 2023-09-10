package net.casual.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.task.Task
import java.util.*
import java.util.function.IntFunction

class TickedScheduler: MinecraftScheduler {
    internal val tasks: Int2ObjectMap<Queue<Task>> = Int2ObjectOpenHashMap()
    internal var tickCount = 0

    fun tick() {
        val queue = this.tasks.remove(this.tickCount++)
        if (queue !== null) {
            queue.forEach(Task::run)
            queue.clear()
        }
    }

    override fun schedule(duration: MinecraftTimeDuration, runnable: Runnable): Task {
        val task = Task.of(runnable)
        this.tasks.computeIfAbsent(this.tickCount + duration.toTicks(), IntFunction { ArrayDeque() }).add(task)
        return task
    }
}