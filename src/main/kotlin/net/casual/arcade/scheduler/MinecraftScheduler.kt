package net.casual.arcade.scheduler

import net.casual.arcade.task.Task

interface MinecraftScheduler {
    /**
     *
     */
    fun schedule(duration: MinecraftTimeDuration, runnable: Runnable): Task

    fun schedule(time: Int, unit: MinecraftTimeUnit, runnable: Runnable): Task {
        return this.schedule(unit.duration(time), runnable)
    }

    fun scheduleInLoop(
        delay: MinecraftTimeDuration,
        interval: MinecraftTimeDuration,
        duration: MinecraftTimeDuration,
        block: Runnable
    ): Task {
        val task = Task.of(block)
        val total = duration + delay
        var current = delay
        while (current < total) {
            this.schedule(current, task)
            current += interval
        }
        return task
    }

    fun scheduleInLoop(
        delay: Int,
        interval: Int,
        duration: Int,
        unit: MinecraftTimeUnit,
        runnable: Runnable
    ): Task {
        return this.scheduleInLoop(
            unit.duration(delay),
            unit.duration(interval),
            unit.duration(duration),
            runnable
        )
    }
}