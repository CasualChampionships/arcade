package net.casual.arcade.gui.task

import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class NameTagTask(
    public val owner: Minigame<*>,
    private val tag: ArcadeNameTag
): Task {
    init {
        this.owner.addNameTag(this.tag)
    }

    final override fun run() {
        this.owner.removeNameTag(this.tag)
    }
}