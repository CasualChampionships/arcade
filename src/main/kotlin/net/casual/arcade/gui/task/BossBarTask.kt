package net.casual.arcade.gui.task

import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class BossBarTask(
    private val minigame: Minigame<*>,
    private val bar: CustomBossBar
): Task {
    init {
        this.minigame.addBossbar(this.bar)
    }

    final override fun run() {
        this.minigame.removeBossbar(this.bar)
    }
}