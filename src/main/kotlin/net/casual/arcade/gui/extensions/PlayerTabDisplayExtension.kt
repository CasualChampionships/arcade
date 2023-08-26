package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.Extension
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.utils.SidebarUtils
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.*
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.ServerScoreboard
import net.minecraft.server.level.ServerPlayer

internal class PlayerTabDisplayExtension(
    private val owner: ServerPlayer
): Extension {
    private var previousHeader: Component? = null
    private var previousFooter: Component? = null
    private var current: ArcadeTabDisplay? = null

    private var ticks = 0

    internal fun tick() {
        val current = this.current ?: return
        if (this.ticks++ % current.interval != 0) {
            return
        }

        val header = current.header.getComponent(this.owner)
        val footer = current.footer.getComponent(this.owner)
        if (header != this.previousHeader || footer != this.previousFooter) {
            this.setDisplay(header, footer)
        }
    }

    internal fun setDisplay(header: Component, footer: Component) {
        this.previousHeader = header
        this.previousFooter = footer
        this.owner.connection.send(ClientboundTabListPacket(header, footer))
    }

    internal fun set(display: ArcadeTabDisplay) {
        val current = this.current
        if (current !== null) {
            current.removePlayer(this.owner)
        }
        this.current = display
        this.ticks = 0
        this.setDisplay(display.header.getComponent(this.owner), display.footer.getComponent(this.owner))
    }

    internal fun remove() {
        this.current = null
        this.previousHeader = null
        this.previousFooter = null
        this.owner.connection.send(ClientboundTabListPacket(Component.empty(), Component.empty()))
    }

    internal fun disconnect() {
        this.current?.removePlayer(this.owner)
    }
}