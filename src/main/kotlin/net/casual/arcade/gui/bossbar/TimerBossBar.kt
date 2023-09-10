package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.TickableUI
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casual.arcade.task.Completable
import net.casual.arcade.task.Task

abstract class TimerBossBar(
    duration: MinecraftTimeDuration
): CustomBossBar(), TickableUI, Completable {
    private val completable = Completable.Impl()

    private var ticks = duration.toTicks()
    private var tick = 0

    override val complete: Boolean
        get() = this.completable.complete

    override fun tick() {
        if (this.tick < this.ticks) {
            this.tick++
            return
        }
        if (!this.complete) {
            this.completable.complete()
        }
    }

    override fun then(task: Task): Completable {
        return this.completable.then(task)
    }

    fun getProgress(): Float {
        return this.tick / this.ticks.toFloat()
    }

    fun getRemainingDuration(): MinecraftTimeDuration {
        return Ticks.duration(this.ticks - this.tick)
    }
}