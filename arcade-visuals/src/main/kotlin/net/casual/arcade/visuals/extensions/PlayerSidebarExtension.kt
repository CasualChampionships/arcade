/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.getExtension
import net.casual.arcade.utils.ScoreboardUtils
import net.casual.arcade.visuals.sidebar.Sidebar
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.DisplaySlot
import java.util.*
import java.util.function.Consumer

internal class PlayerSidebarExtension(
    owner: ServerPlayer
): PlayerExtension(owner) {
    private val previousRows = ArrayList<SidebarComponent>()
    private var previousTitle: Component? = null
    private var current: Sidebar? = null

    private var ticks = 0

    internal fun tick() {
        val current = this.current ?: return
        if (this.ticks++ % current.interval != 0) {
            return
        }

        val title = current.title.get(this.player)
        if (title != this.previousTitle) {
            this.setTitle(title)
        }

        var max = 0
        current.forEachRow(this.player) { index, replacement ->
            max = index
            val previous = this.previousRows.getOrElse(index) {
                this.previousRows.add(index, replacement)
                sendSetScorePacket(this.player, index, replacement)
                return@forEachRow
            }
            if (previous != replacement) {
                this.previousRows[index] = replacement
                sendSetScorePacket(this.player, index, replacement)
            }
        }
        if (max < this.previousRows.lastIndex) {
            for (index in this.previousRows.lastIndex downTo (max + 1)) {
                this.previousRows.removeAt(index)
                sendResetScorePacket(this.player, index)
            }
        }
    }

    internal fun setTitle(title: Component) {
        this.previousTitle = title
        sendSetObjectivePacket(this.player, METHOD_CHANGE, title)
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

        sendResetScorePacket(this.player, this.previousRows.size)

        this.resendRows(index)
    }

    internal fun set(sidebar: Sidebar) {
        val current = this.current
        if (current !== null) {
            current.removePlayer(this.player)
        }
        this.current = sidebar
        this.ticks = 0

        sendSetObjectivePacket(this.player, METHOD_ADD, sidebar.title.get(this.player))
        sendSetSidebarDisplayPacket(this.player, false)
        sidebar.forEachRow(this.player, this::addRow)
    }

    internal fun remove() {
        this.current = null
        this.previousTitle = null
        this.previousRows.clear()
        sendSetObjectivePacket(this.player, METHOD_REMOVE)
        sendSetSidebarDisplayPacket(this.player, true)
    }

    internal fun resend(sender: Consumer<Packet<ClientGamePacketListener>>) {
        val sidebar = this.current ?: return
        val title = this.previousTitle ?: sidebar.title.get(this.player)
        sendSetObjectivePacket(this.player, METHOD_ADD, title, sender)
        sendSetSidebarDisplayPacket(this.player, false, sender)
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
            sendSetScorePacket(this.player, i, this.previousRows[i], sender)
        }
    }

    companion object {
        private const val OBJECTIVE_NAME = "Z\$DummyObjective"
        private val objective = ScoreboardUtils.dummyObjective(OBJECTIVE_NAME)
        private val players = ArrayList<String>(16)

        internal val ServerPlayer.sidebar
            get() = this.getExtension<PlayerSidebarExtension>()

        private fun sendSetObjectivePacket(
            player: ServerPlayer,
            method: Int,
            title: Component? = null,
            sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
        ) {
            if (title !== null) {
                objective.displayName = title
            }
            val packet = ClientboundSetObjectivePacket(objective, method)
            sender.accept(packet)
        }

        private fun sendSetScorePacket(
            player: ServerPlayer,
            index: Int,
            component: SidebarComponent,
            sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
        ) {
            val packet = ClientboundSetScorePacket(
                players[index],
                OBJECTIVE_NAME,
                index,
                Optional.ofNullable(component.display),
                Optional.ofNullable(component.score)
            )
            sender.accept(packet)
        }

        private fun sendResetScorePacket(
            player: ServerPlayer,
            index: Int,
            sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
        ) {
            sender.accept(ClientboundResetScorePacket(players[index], OBJECTIVE_NAME))
        }

        private fun sendSetSidebarDisplayPacket(
            player: ServerPlayer,
            remove: Boolean,
            sender: Consumer<Packet<ClientGamePacketListener>> = Consumer(player.connection::send)
        ) {
            val objective = if (remove) null else objective
            sender.accept(ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, objective))
        }

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<ServerLoadedEvent> {
                for (i in 0..15) {
                    players.add("\$D${i.toString(16)}")
                }
            }
            GlobalEventHandler.Server.register<PlayerExtensionEvent> { event ->
                event.addExtension(::PlayerSidebarExtension)
            }
            GlobalEventHandler.Server.register<PlayerLeaveEvent> { (player) ->
                player.sidebar.disconnect()
            }
            GlobalEventHandler.Server.register<PlayerTickEvent> { (player) ->
                player.sidebar.tick()
            }
        }
    }
}