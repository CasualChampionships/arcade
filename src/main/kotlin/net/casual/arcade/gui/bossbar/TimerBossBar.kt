package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.TickableUI
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.task.Completable
import net.casual.arcade.task.Task
import net.casual.arcade.utils.TimeUtils.Ticks
import net.minecraft.server.level.ServerPlayer

public abstract class TimerBossBar(
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

    public fun getProgress(): Float {
        return this.tick / this.ticks.toFloat()
    }

    public fun getRemainingDuration(): MinecraftTimeDuration {
        return (this.ticks - this.tick).Ticks
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