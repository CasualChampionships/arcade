package net.casualuhc.arcade.scheduler

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.Ticks

@Suppress("unused")
object GlobalTickedScheduler {
    private val scheduler = TickedScheduler()

    init {
        GlobalEventHandler.register<ServerTickEvent> { this.scheduler.tick() }
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