package net.casual.arcade.gui.task

import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class TabDisplayTask(
    private val minigame: Minigame<*>,
    display: ArcadeTabDisplay
): Task {
    init {
        this.minigame.setTabDisplay(display)
    }

    override fun run() {
        this.minigame.removeTabDisplay()
    }
}