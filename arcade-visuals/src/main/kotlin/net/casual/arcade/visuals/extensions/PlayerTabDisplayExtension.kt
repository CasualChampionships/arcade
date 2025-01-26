/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.getExtension
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

internal class PlayerTabDisplayExtension(
    owner: ServerPlayer
): PlayerExtension(owner) {
    private var previousHeader: Component? = null
    private var previousFooter: Component? = null
    private var current: PlayerListDisplay? = null

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

    internal fun set(display: PlayerListDisplay) {
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

    companion object {
        internal val ServerPlayer.tabDisplay
            get() = this.getExtension<PlayerTabDisplayExtension>()

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> { event ->
                event.addExtension(::PlayerTabDisplayExtension)
            }
            GlobalEventHandler.Server.register<PlayerLeaveEvent> { (player) ->
                player.tabDisplay.disconnect()
            }
            GlobalEventHandler.Server.register<PlayerTickEvent> { (player) ->
                player.tabDisplay.tick()
            }
        }
    }
}