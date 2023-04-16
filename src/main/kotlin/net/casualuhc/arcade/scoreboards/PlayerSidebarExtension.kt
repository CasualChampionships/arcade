package net.casualuhc.arcade.scoreboards

import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.utils.SidebarUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.*
import net.minecraft.server.ServerScoreboard
import net.minecraft.server.level.ServerPlayer

class PlayerSidebarExtension(
    private val owner: ServerPlayer
): Extension {
    private val previousRows = ArrayList<Component>()
    private var previousTitle: Component? = null
    private var current: ArcadeSidebar? = null

    private var ticks = 0

    internal fun tick() {
        val current = this.current ?: return
        if (this.ticks++ % current.interval != 0) {
            return
        }

        val title = current.title.getComponent(this.owner)
        if (title != this.previousTitle) {
            this.setTitle(title)
        }

        for ((index, previous) in this.previousRows.withIndex()) {
            val replacement = current.getRow(index).getComponent(this.owner)
            if (previous == replacement) {
                continue
            }
            SidebarUtils.sendPlayerTeamUpdatePacket(this.owner, index, false, replacement)
            SidebarUtils.sendSetScorePacket(this.owner, index, ServerScoreboard.Method.CHANGE)
        }
    }

    internal fun setTitle(title: Component) {
        this.previousTitle = title
        SidebarUtils.sendSetObjectivePacket(this.owner, METHOD_CHANGE, title)
    }

    internal fun addRow(index: Int, component: Component) {
        this.previousRows.add(index, component)
        for (i in index until this.previousRows.size) {
            SidebarUtils.sendPlayerTeamUpdatePacket(this.owner, i, true, this.previousRows[i])
            SidebarUtils.sendSetScorePacket(this.owner, i, ServerScoreboard.Method.CHANGE)
        }
    }

    internal fun setRow(index: Int, component: Component) {
        this.previousRows[index] = component
        for (i in index until this.previousRows.size) {
            SidebarUtils.sendPlayerTeamUpdatePacket(this.owner, index, false, component)
            SidebarUtils.sendSetScorePacket(this.owner, index, ServerScoreboard.Method.CHANGE)
        }
    }

    internal fun removeRow(index: Int) {
        this.previousRows.removeAt(index)

        SidebarUtils.sendSetScorePacket(this.owner, this.previousRows.size, ServerScoreboard.Method.REMOVE)
        SidebarUtils.sendPlayerTeamRemovePacket(this.owner, this.previousRows.size)

        for (i in index until this.previousRows.size) {
            SidebarUtils.sendPlayerTeamUpdatePacket(this.owner, i, false, this.previousRows[i])
            SidebarUtils.sendSetScorePacket(this.owner, i, ServerScoreboard.Method.CHANGE)
        }
    }

    internal fun set(sidebar: ArcadeSidebar) {
        val current = this.current
        if (current !== null) {
            current.removePlayer(this.owner)
            this.remove()
        }
        this.current = sidebar
        this.ticks = 0

        SidebarUtils.sendSetObjectivePacket(this.owner, METHOD_ADD, sidebar.title.getComponent(this.owner))
        SidebarUtils.sendSetDisplayPacket(this.owner, false)
        for (i in 0 until sidebar.size()) {
            this.addRow(i, sidebar.getRow(i).getComponent(this.owner))
        }
    }

    internal fun remove() {
        this.current = null
        this.previousTitle = null
        this.previousRows.clear()
        SidebarUtils.sendSetObjectivePacket(this.owner, METHOD_REMOVE)
        SidebarUtils.sendSetDisplayPacket(this.owner, true)
    }

    internal fun disconnect() {
        this.current?.removePlayer(this.owner)
    }
}