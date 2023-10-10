package net.casual.arcade.gui.task

import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class NameTagTask(
    private val minigame: Minigame<*>,
    private val tag: ArcadeNameTag
): Task {
    init {
        this.minigame.addNameTag(this.tag)
    }

    final override fun run() {
        this.minigame.removeNameTag(this.tag)
    }
}