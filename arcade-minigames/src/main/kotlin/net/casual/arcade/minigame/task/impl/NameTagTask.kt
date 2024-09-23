package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.visuals.nametag.PlayerNameTag

public open class NameTagTask(
    private val minigame: Minigame<*>,
    private val tag: PlayerNameTag
): Task {
    init {
        this.minigame.ui.addNameTag(this.tag)
    }

    final override fun run() {
        this.minigame.ui.removeNameTag(this.tag)
    }
}