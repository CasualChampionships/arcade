package net.casualuhc.arcade.scheduler

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.Ticks

@Suppress("unused")
object Scheduler {
    private val impl = TaskScheduler()

    init {
        EventHandler.register<ServerTickEvent> { this.impl.tick() }
    }

    @JvmStatic
    fun schedule(time: Int, unit: MinecraftTimeUnit = Ticks, task: Runnable): Task {
        return this.impl.schedule(time, unit, task)
    }

    @JvmStatic
    fun schedule(time: Int, unit: MinecraftTimeUnit, task: Task): Task {
        return this.impl.schedule(time, unit, task)
    }

    @JvmStatic
    fun scheduleInLoop(delay: Int, interval: Int, duration: Int, unit: MinecraftTimeUnit = Ticks, block: Runnable): Task {
        return this.impl.scheduleInLoop(delay, interval, duration, unit, block)
    }
}