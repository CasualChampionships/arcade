package net.casual.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.*
import java.util.function.IntFunction

open class TickedScheduler {
    val tasks: Int2ObjectMap<Queue<Task>> = Int2ObjectOpenHashMap()
    var tickCount = 0

    fun tick() {
        val queue = this.tasks.remove(this.tickCount++)
        if (queue !== null) {
            queue.forEach(Task::run)
            queue.clear()
        }
    }

    fun schedule(time: Int, unit: MinecraftTimeUnit, task: Runnable): Task {
        return this.schedule(time, unit, Task.of(task))
    }

    fun schedule(time: Int, unit: MinecraftTimeUnit, task: Task): Task {
        require(time >= 0) { "Cannot schedule a task in the past!" }
        this.tasks.computeIfAbsent(this.tickCount + unit.toTicks(time), IntFunction { ArrayDeque() }).add(task)
        return task
    }

    fun scheduleInLoop(delay: Int, interval: Int, duration: Int, unit: MinecraftTimeUnit, block: Runnable): Task {
        require(delay >= 0 && interval > 0 && duration >= 0) {
            "Delay, interval or duration ticks cannot be negative"
        }
        val task = Task.of(block)
        var tick = delay
        while (tick < duration + delay) {
            this.schedule(tick, unit, task)
            tick += interval
        }
        return task
    }
}