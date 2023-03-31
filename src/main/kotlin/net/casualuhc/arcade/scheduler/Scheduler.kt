package net.casualuhc.arcade.scheduler

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerTickEvent
import java.util.*
import java.util.function.IntFunction

object Scheduler: EventListener {
    private val tasks = Int2ObjectOpenHashMap<Queue<Task>>()
    private var ticks = 0

    init {
        EventHandler.register<ServerTickEvent> {
            val queue = this.tasks.remove(this.ticks++)
            if (queue !== null) {
                queue.forEach(Task::run)
                queue.clear()
            }
        }
    }

    @JvmStatic
    fun schedule(time: Int, unit: MinecraftTimeUnit = MinecraftTimeUnit.Ticks, task: () -> Unit): Task {
        return this.schedule(time, unit, Task(task))
    }

    @JvmStatic
    fun schedule(time: Int, unit: MinecraftTimeUnit, task: Task): Task {
        require(time >= 0) { "Cannot schedule a task in the past!" }
        this.tasks.computeIfAbsent(this.ticks + unit.toTicks(time), IntFunction { ArrayDeque() }).add(task)
        return task
    }

    @JvmStatic
    fun scheduleInLoop(delay: Int, interval: Int, duration: Int, unit: MinecraftTimeUnit = MinecraftTimeUnit.Ticks, block: () -> Unit) {
        require(delay >= 0 && interval > 0 && duration >= 0) { "Delay, interval or duration ticks cannot be negative" }
        val task = Task(block)
        var tick = delay
        while (tick < interval + delay) {
            this.schedule(tick, unit, task)
            tick += interval
        }
    }
}