package net.casual.arcade.minigame

import net.casual.arcade.scheduler.MinecraftScheduler
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.task.Task

class MinigameScheduler internal constructor(): MinecraftScheduler {
    internal val minigame = TickedScheduler()
    internal val phased = TickedScheduler()

    internal fun tick() {
        this.minigame.tick()
        this.phased.tick()
    }

    override fun schedule(duration: MinecraftTimeDuration, runnable: Runnable): Task {
        return this.minigame.schedule(duration, runnable)
    }

    override fun schedule(time: Int, unit: MinecraftTimeUnit, runnable: Runnable): Task {
        return this.minigame.schedule(time, unit, runnable)
    }

    override fun scheduleInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        block: Runnable
    ): Task {
        return this.minigame.scheduleInLoop(delay, interval, duration, block)
    }

    override fun scheduleInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        runnable: Runnable
    ): Task {
        return this.minigame.scheduleInLoop(delay, interval, duration, unit, runnable)
    }

    fun schedulePhased(duration: MinecraftTimeDuration, runnable: Runnable): Task {
        return this.phased.schedule(duration, runnable)
    }

    fun schedulePhased(time: Int, unit: MinecraftTimeUnit, runnable: Runnable): Task {
        return this.phased.schedule(time, unit, runnable)
    }

    fun schedulePhasedInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        block: Runnable
    ): Task {
        return this.phased.scheduleInLoop(delay, interval, duration, block)
    }

    fun schedulePhasedInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        runnable: Runnable
    ): Task {
        return this.phased.scheduleInLoop(delay, interval, duration, unit, runnable)
    }
}