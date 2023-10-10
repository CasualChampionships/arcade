package net.casual.arcade.gui.task

import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class BossBarTask(
    public val owner: Minigame<*>,
    private val bar: CustomBossBar
): Task {
    init {
        this.owner.addBossbar(this.bar)
    }

    final override fun run() {
        this.owner.removeBossbar(this.bar)
    }
}