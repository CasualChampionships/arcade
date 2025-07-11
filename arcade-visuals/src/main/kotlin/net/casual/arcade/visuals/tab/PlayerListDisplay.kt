/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.tab

import com.mojang.authlib.GameProfile
import net.casual.arcade.utils.ClientboundPlayerInfoUpdatePacket
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.casual.arcade.visuals.core.TrackedPlayerUI
import net.casual.arcade.visuals.core.TickableUI
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.extensions.PlayerTabDisplayExtension.Companion.tabDisplay
import net.casual.arcade.visuals.tab.PlayerListEntries.Entry
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.GameType
import java.util.*
import java.util.function.Consumer

public open class PlayerListDisplay(
    private val display: PlayerListEntries
): TrackedPlayerUI(), TickableUI {
    private val previous = ArrayList<Entry>()

    public var header: PlayerSpecificElement<Component> = ComponentElements.empty()
        protected set
    public var footer: PlayerSpecificElement<Component> = ComponentElements.empty()
        protected set

    public open fun setDisplay(header: PlayerSpecificElement<Component>, footer: PlayerSpecificElement<Component>) {
        this.header = header
        this.footer = footer

        for (player in this.getPlayers()) {
            player.tabDisplay.setDisplay(header.get(player), footer.get(player))
        }
    }

    public override fun tick(server: MinecraftServer) {
        this.header.tick(server)
        this.footer.tick(server)
        this.display.tick(server)

        // We try to be as efficient as possible with these packets
        val removing = ClientboundPlayerInfoRemovePacket(ArrayList())
        val entries = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        val uuids = removing.profileIds

        val size = this.display.size
        val previousSize = this.previous.size
        if (size > previousSize) {
            for (i in previousSize..< size) {
                this.previous.add(Entry.DEFAULT)
            }
        } else if (size < previousSize) {
            this.previous.subList(size, previousSize).clear()
            for (i in size..< previousSize) {
                uuids.add(this.createUUIDForIndex(i))
            }
        }

        for (i in 0..< size) {
            val entry = this.checkEntryUpdate(i) ?: continue
            val clientbound = this.toClientboundEntry(i, entry)
            entries.add(clientbound)
            if (i < previousSize) {
                uuids.add(clientbound.profileId)
            }
        }

        if (uuids.isNotEmpty()) {
            this.sendToAllPlayers(removing)
        }
        if (entries.isNotEmpty()) {
            this.sendToAllPlayers(ClientboundPlayerInfoUpdatePacket(EnumUtils.completeSet(), entries))
        }
    }

    override fun shouldTickWhenPaused(): Boolean {
        return true
    }

    public open fun replacePlayerInfoUpdatePacket(
        receiver: ServerPlayer,
        packet: ClientboundPlayerInfoUpdatePacket
    ): ClientboundPlayerInfoUpdatePacket {
        if (!this.hasPlayer(receiver)) {
            return packet
        }

        if (packet.actions().contains(Action.UPDATE_LISTED)) {
            val mapped = packet.entries().map { entry ->
                if (!this.isIndexUUID(entry.profileId)) {
                    ClientboundPlayerInfoUpdatePacket.Entry(
                        entry.profileId,
                        entry.profile,
                        false,
                        entry.latency,
                        entry.gameMode,
                        entry.displayName,
                        entry.showHat,
                        entry.listOrder,
                        entry.chatSession
                    )
                } else {
                    entry
                }
            }
            return ClientboundPlayerInfoUpdatePacket(packet.actions(), mapped)
        }
        return packet
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.tabDisplay.set(this)

        this.resendTo(player, PlayerConnectionConsumer(player.connection))
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.tabDisplay.remove()

        this.unsendTo(player, PlayerConnectionConsumer(player.connection))
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        if (sender !is PlayerConnectionConsumer) {
            player.tabDisplay.resend(sender)
        }

        val hiding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (other in player.levelServer.playerList.players) {
            hiding.add(this.hidingClientboundEntry(other, true))
        }
        sender.accept(ClientboundPlayerInfoUpdatePacket(EnumSet.of(Action.UPDATE_LISTED), hiding))

        val adding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (i in 0..< this.display.size) {
            val entry = this.display.getEntryAt(i)
            adding.add(this.toClientboundEntry(i, entry))
        }
        sender.accept(ClientboundPlayerInfoUpdatePacket(EnumUtils.completeSet(), adding))
    }

    protected fun unsendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        val hiding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (other in player.levelServer.playerList.players) {
            hiding.add(this.hidingClientboundEntry(other, false))
        }
        sender.accept(ClientboundPlayerInfoUpdatePacket(EnumSet.of(Action.UPDATE_LISTED), hiding))

        val size = this.display.size
        val removing = ArrayList<UUID>(size)
        for (i in 0..< size) {
            removing.add(this.createUUIDForIndex(i))
        }
        sender.accept(ClientboundPlayerInfoRemovePacket(removing))
        sender.accept(ClientboundTabListPacket(Component.empty(), Component.empty()))
    }

    private fun checkEntryUpdate(index: Int): Entry? {
        val entry = this.display.getEntryAt(index)
        val previous = this.previous[index]
        this.previous[index] = entry

        if (entry.textures != previous.textures) {
            // The entire entry needs to be resent
            return entry
        }

        val actions = EnumUtils.emptySet<Action>()
        if (entry.latency != previous.latency) {
            actions.add(Action.UPDATE_LATENCY)
        }
        if (entry.display != previous.display) {
            actions.add(Action.UPDATE_DISPLAY_NAME)
        }

        if (actions.isNotEmpty()) {
            val entries = listOf(ClientboundPlayerInfoUpdatePacket.Entry(
                this.createUUIDForIndex(index),
                // We don't need to calculate the GP
                null,
                true,
                entry.latency,
                GameType.SURVIVAL,
                entry.display,
                entry.showHat,
                -index, // For some reason, Mojang does this in reverse
                null
            ))
            this.sendToAllPlayers(ClientboundPlayerInfoUpdatePacket(actions, entries))
        }
        return null
    }

    private fun hidingClientboundEntry(player: ServerPlayer, hidden: Boolean): ClientboundPlayerInfoUpdatePacket.Entry {
        return ClientboundPlayerInfoUpdatePacket.Entry(
            player.uuid, null, !hidden, 0, GameType.SURVIVAL, null, true, 0, null
        )
    }

    private fun toClientboundEntry(index: Int, entry: Entry): ClientboundPlayerInfoUpdatePacket.Entry {
        val profile = this.createProfileForIndex(index)
        profile.properties.put("textures", entry.textures.toProperty())
        return ClientboundPlayerInfoUpdatePacket.Entry(
            profile.id,
            profile,
            true,
            entry.latency,
            GameType.SURVIVAL,
            entry.display,
            entry.showHat,
            -index,
            null
        )
    }

    private fun isIndexUUID(uuid: UUID): Boolean {
        return uuid.leastSignificantBits == 0L && uuid.mostSignificantBits >= 31
    }

    private fun createUUIDForIndex(index: Int): UUID {
        // We never want to create the NIL uuid, so we shift by some integer
        return UUID(index.toLong() + 31, 0)
    }

    private fun createProfileForIndex(index: Int): GameProfile {
        val char = (0x00B4 + index).toChar()
        return GameProfile(
            this.createUUIDForIndex(index),
            char.toString()
        )
    }

    private class PlayerConnectionConsumer(
        val connection: ServerGamePacketListenerImpl
    ): Consumer<Packet<ClientGamePacketListener>> {
        override fun accept(packet: Packet<ClientGamePacketListener>) {
            this.connection.send(packet)
        }
    }
}