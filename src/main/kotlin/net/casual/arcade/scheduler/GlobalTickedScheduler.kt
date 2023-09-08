package net.casual.arcade.scheduler

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks

object GlobalTickedScheduler {
    private val scheduler = TickedScheduler()

    init {
        GlobalEventHandler.register<ServerTickEvent> { this.scheduler.tick() }
    }

    @JvmStatic
    fun later(task: Runnable) {
        this.schedule(time = 0, task = task)
    }

    @JvmStatic
    fun schedule(time: Int, unit: MinecraftTimeUnit = Ticks, task: Runnable): Task {
        return this.scheduler.schedule(time, unit, task)
    }

    @JvmStatic
    fun schedule(time: Int, unit: MinecraftTimeUnit, task: Task): Task {
        return this.scheduler.schedule(time, unit, task)
    }

    @JvmStatic
    fun scheduleInLoop(delay: Int, interval: Int, duration: Int, unit: MinecraftTimeUnit = Ticks, block: Runnable): Task {
        return this.scheduler.scheduleInLoop(delay, interval, duration, unit, block)
    }
}