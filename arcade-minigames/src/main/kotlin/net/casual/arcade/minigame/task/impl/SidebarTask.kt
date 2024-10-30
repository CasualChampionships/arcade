package net.casual.arcade.minigame.task.impl

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.visuals.sidebar.Sidebar

public open class SidebarTask(
    private val minigame: Minigame,
    sidebar: Sidebar
): Task {
    init {
        this.minigame.ui.setSidebar(sidebar)
    }

    override fun run() {
        this.minigame.ui.removeSidebar()
    }
}