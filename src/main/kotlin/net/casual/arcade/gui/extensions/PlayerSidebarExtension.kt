package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.utils.SidebarUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.function.Consumer

internal class PlayerSidebarExtension(
    owner: ServerGamePacketListenerImpl
): PlayerExtension(owner) {
    private val previousRows = ArrayList<SidebarComponent>()
    private var previousTitle: Component? = null
    private var current: ArcadeSidebar? = null

    private var ticks = 0

    internal fun tick() {
        val current = this.current ?: return
        if (this.ticks++ % current.interval != 0) {
            return
        }

        val title = current.title.getComponent(this.player)
        if (title != this.previousTitle) {
            this.setTitle(title)
        }

        for ((index, previous) in this.previousRows.withIndex()) {
            val replacement = current.getRow(index).getComponent(this.player)
            if (previous == replacement) {
                continue
            }
            this.previousRows[index] = replacement
            SidebarUtils.sendSetScorePacket(this.player, index, replacement)
        }
    }

    internal fun setTitle(title: Component) {
        this.previousTitle = title
        SidebarUtils.sendSetObjectivePacket(this.player, METHOD_CHANGE, title)
    }

    internal fun addRow(index: Int, component: SidebarComponent) {
        this.previousRows.add(index, component)
        this.resendRows(index)
    }

    internal fun setRow(index: Int, component: SidebarComponent) {
        this.previousRows[index] = component
        this.resendRows(index)
    }

    internal fun removeRow(index: Int) {
        this.previousRows.removeAt(index)

        SidebarUtils.sendResetScorePacket(this.player, this.previousRows.size)

        this.resendRows(index)
    }

    internal fun set(sidebar: ArcadeSidebar) {
        val current = this.current
        if (current !== null) {
            current.removePlayer(this.player)
        }
        this.current = sidebar
        this.ticks = 0

        SidebarUtils.sendSetObjectivePacket(this.player, METHOD_ADD, sidebar.title.getComponent(this.player))
        SidebarUtils.sendSetSidebarDisplayPacket(this.player, false)
        for (i in 0 until sidebar.size()) {
            this.addRow(i, sidebar.getRow(i).getComponent(this.player))
        }
    }

    internal fun remove() {
        this.current = null
        this.previousTitle = null
        this.previousRows.clear()
        SidebarUtils.sendSetObjectivePacket(this.player, METHOD_REMOVE)
        SidebarUtils.sendSetSidebarDisplayPacket(this.player, true)
    }

    internal fun resend(sender: Consumer<Packet<ClientGamePacketListener>>) {
        val sidebar = this.current ?: return
        val title = this.previousTitle ?: sidebar.title.getComponent(this.player)
        SidebarUtils.sendSetObjectivePacket(this.player, METHOD_ADD, title, sender)
        SidebarUtils.sendSetSidebarDisplayPacket(this.player, false, sender)
        this.resendRows(0, sender)
    }

    internal fun disconnect() {
        this.current?.removePlayer(this.player)
    }

    private fun resendRows(
        from: Int,
        sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(this.player.connection::send)
    ) {
        for (i in from until this.previousRows.size) {
            SidebarUtils.sendSetScorePacket(this.player, i, this.previousRows[i], sender)
        }
    }
}