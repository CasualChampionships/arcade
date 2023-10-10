package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.TickableUI
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
import net.casual.arcade.task.Task
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.server.level.ServerPlayer

public abstract class TimerBossBar: CustomBossBar(), TickableUI, Completable {
    private val completable = Completable.Impl()

    private var ticks = -1
    private var tick = 0

    override val complete: Boolean
        get() = this.completable.complete

    override fun tick() {
        if (this.ticks == -1) {
            return
        }
        if (this.tick < this.ticks) {
            this.tick++
            return
        }
        this.completable.complete()
    }

    override fun then(task: Task): Completable {
        return this.completable.then(task)
    }

    public fun setDuration(duration: MinecraftTimeDuration) {
        this.completable.complete = false
        this.tick = 0
        this.ticks = duration.toTicks()
    }

    public fun getProgress(): Float {
        return if (this.ticks == -1) 0.0F else this.tick / this.ticks.toFloat()
    }

    public fun getRemainingDuration(): MinecraftTimeDuration {
        return if (this.ticks == -1) 0.Ticks else (this.ticks - this.tick).Ticks
    }

    /**
     * This gets the progress of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    override fun getProgress(player: ServerPlayer): Float {
        return this.getProgress()
    }
}