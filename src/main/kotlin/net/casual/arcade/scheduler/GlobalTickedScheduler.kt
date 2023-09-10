package net.casual.arcade.scheduler

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.task.Task

object GlobalTickedScheduler {
    private val scheduler = TickedScheduler()

    init {
        GlobalEventHandler.register<ServerTickEvent> { this.scheduler.tick() }
    }

    @JvmStatic
    fun later(task: Runnable) {
        this.schedule(MinecraftTimeDuration.ZERO, task)
    }

    fun schedule(duration: MinecraftTimeDuration, runnable: Runnable): Task {
        return this.scheduler.schedule(duration, runnable)
    }

    fun schedule(time: Int, unit: MinecraftTimeUnit, runnable: Runnable): Task {
        return this.scheduler.schedule(time, unit, runnable)
    }

    fun scheduleInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        block: Runnable
    ): Task {
        return this.scheduler.scheduleInLoop(delay, interval, duration, block)
    }

    fun scheduleInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        runnable: Runnable
    ): Task {
        return this.scheduler.scheduleInLoop(delay, interval, duration, unit, runnable)
    }
}