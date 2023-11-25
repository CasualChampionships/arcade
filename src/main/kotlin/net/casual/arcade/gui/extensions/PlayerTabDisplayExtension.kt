package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.network.ServerGamePacketListenerImpl

internal class PlayerTabDisplayExtension(
    owner: ServerGamePacketListenerImpl
): PlayerExtension(owner) {
    private var previousHeader: Component? = null
    private var previousFooter: Component? = null
    private var current: ArcadeTabDisplay? = null

    private var ticks = 0

    internal fun tick() {
        val current = this.current ?: return
        if (this.ticks++ % current.interval != 0) {
            return
        }

        val header = current.header.getComponent(this.player)
        val footer = current.footer.getComponent(this.player)
        if (header != this.previousHeader || footer != this.previousFooter) {
            this.setDisplay(header, footer)
        }
    }

    internal fun setDisplay(header: Component, footer: Component) {
        this.previousHeader = header
        this.previousFooter = footer
        this.player.connection.send(ClientboundTabListPacket(header, footer))
    }

    internal fun set(display: ArcadeTabDisplay) {
        val current = this.current
        if (current !== null) {
            current.removePlayer(this.player)
        }
        this.current = display
        this.ticks = 0
        this.setDisplay(display.header.getComponent(this.player), display.footer.getComponent(this.player))
    }

    internal fun remove() {
        this.current = null
        this.previousHeader = null
        this.previousFooter = null
        this.player.connection.send(ClientboundTabListPacket(Component.empty(), Component.empty()))
    }

    internal fun disconnect() {
        this.current?.removePlayer(this.player)
    }
}