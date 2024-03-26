package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.gui.tab.ArcadePlayerListDisplay
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.function.Consumer

internal class PlayerTabDisplayExtension(
    owner: ServerGamePacketListenerImpl
): PlayerExtension(owner) {
    private var previousHeader: Component? = null
    private var previousFooter: Component? = null
    private var current: ArcadePlayerListDisplay? = null

    private var ticks = 0

    internal fun tick() {
        val current = this.current ?: return
        if (this.ticks++ % current.interval != 0) {
            return
        }

        val header = current.header.get(this.player)
        val footer = current.footer.get(this.player)
        if (header != this.previousHeader || footer != this.previousFooter) {
            this.setDisplay(header, footer)
        }
    }

    internal fun setDisplay(header: Component, footer: Component) {
        this.previousHeader = header
        this.previousFooter = footer
        this.player.connection.send(ClientboundTabListPacket(header, footer))
    }

    internal fun set(display: ArcadePlayerListDisplay) {
        val current = this.current
        if (current !== null) {
            current.removePlayer(this.player)
        }
        this.current = display
        this.ticks = 0
        this.setDisplay(display.header.get(this.player), display.footer.get(this.player))
    }

    internal fun remove() {
        this.current = null
        this.previousHeader = null
        this.previousFooter = null
        this.player.connection.send(ClientboundTabListPacket(Component.empty(), Component.empty()))
    }

    internal fun resend(sender: Consumer<Packet<ClientGamePacketListener>>) {
        val tab = this.current ?: return
        val header = this.previousHeader ?: tab.header.get(this.player)
        val footer = this.previousFooter ?: tab.footer.get(this.player)
        sender.accept(ClientboundTabListPacket(header, footer))
    }

    internal fun disconnect() {
        this.current?.removePlayer(this.player)
    }
}