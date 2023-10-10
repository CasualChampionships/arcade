package net.casual.arcade.gui.task

import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.Task

public open class SidebarTask(
    public val owner: Minigame<*>,
    sidebar: ArcadeSidebar
): Task {
    init {
        this.owner.setSidebar(sidebar)
    }

    override fun run() {
        this.owner.removeSidebar()
    }
}