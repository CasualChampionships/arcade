package net.casual.arcade.gui.task

import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class TabDisplayTask(
    public val owner: Minigame<*>,
    display: ArcadeTabDisplay
): Task {
    init {
        this.owner.setTabDisplay(display)
    }

    override fun run() {
        this.owner.removeTabDisplay()
    }
}