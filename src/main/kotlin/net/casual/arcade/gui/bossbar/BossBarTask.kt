package net.casual.arcade.gui.bossbar

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.Task

abstract class BossBarTask(
    val owner: Minigame,
    private val bar: CustomBossBar
): Task {
    init {
        this.owner.addBossbar(this.bar)
    }

    override fun run() {
        this.owner.removeBossbar(this.bar)
    }
}