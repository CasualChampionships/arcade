package net.casual.arcade.minigame.task.impl

import net.casual.arcade.gui.tab.ArcadePlayerListDisplay
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class PlayerListTask(
    private val minigame: Minigame<*>,
    display: ArcadePlayerListDisplay
): Task {
    init {
        this.minigame.ui.setPlayerListDisplay(display)
    }

    override fun run() {
        this.minigame.ui.removePlayerListDisplay()
    }
}