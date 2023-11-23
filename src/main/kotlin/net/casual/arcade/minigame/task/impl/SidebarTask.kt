package net.casual.arcade.minigame.task.impl

import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class SidebarTask(
    private val minigame: Minigame<*>,
    sidebar: ArcadeSidebar
): Task {
    init {
        this.minigame.ui.setSidebar(sidebar)
    }

    override fun run() {
        this.minigame.ui.removeSidebar()
    }
}