package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.bossbar.CustomBossbar
import net.casual.arcade.visuals.bossbar.TimerBossbar

public open class BossBarTask<T: CustomBossbar>(
    private val minigame: Minigame<*>,
    public val bar: T
): Task {
    init {
        this.minigame.ui.addBossbar(this.bar)
    }

    final override fun run() {
        this.minigame.ui.removeBossbar(this.bar)
    }

    public companion object {
        public fun <T: TimerBossbar> BossBarTask<T>.withDuration(duration: MinecraftTimeDuration): BossBarTask<T> {
            this.bar.setDuration(duration)
            return this
        }

        public fun <T: TimerBossbar> BossBarTask<T>.withRemainingDuration(duration: MinecraftTimeDuration): BossBarTask<T> {
            this.bar.setRemainingDuration(duration)
            return this
        }

        public fun <T: TimerBossbar> BossBarTask<T>.readData(context: TaskCreationContext): BossBarTask<T> {
            this.bar.readData(context)
            return this
        }

        public fun <T: TimerBossbar> BossBarTask<T>.then(task: Task): BossBarTask<T> {
            this.bar.then(task)
            return this
        }
    }
}