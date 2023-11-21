package net.casual.arcade.gui.task

import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class BossBarTask<T: CustomBossBar>(
    private val minigame: Minigame<*>,
    public val bar: T
): Task {
    init {
        this.minigame.ui.addBossbar(this.bar)
    }

    final override fun run() {
        this.minigame.ui.removeBossbar(this.bar)
    }
}