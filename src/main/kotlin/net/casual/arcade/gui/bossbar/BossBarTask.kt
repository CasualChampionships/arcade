package net.casual.arcade.gui.bossbar

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.CancellableTask

abstract class BossBarTask(
    val owner: Minigame,
    private val bar: CustomBossBar
): CancellableTask {
    private var cancel = false

    init {
        this.owner.addBossbar(this.bar)
    }

    final override fun isCancelled(): Boolean {
        return this.cancel
    }

    override fun run() {
        this.owner.removeBossbar(this.bar)
        this.cancel = true
    }
}