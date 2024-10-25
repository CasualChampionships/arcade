package net.casual.arcade.visuals.tab

import com.mojang.authlib.GameProfile
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.tab.PlayerListEntries.Entry
import net.casual.arcade.utils.ClientboundPlayerInfoUpdatePacket
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.visuals.core.PlayerUI
import net.casual.arcade.visuals.core.TickableUI
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.extensions.PlayerTabDisplayExtension.Companion.tabDisplay
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.GameType
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

// TODO: Use the list order functionality?
public class PlayerListDisplay(
    private val display: PlayerListEntries
): PlayerUI(), TickableUI {
    private val previous = ArrayList<Entry>()

    public var header: PlayerSpecificElement<Component> = ComponentElements.empty()
        private set
    public var footer: PlayerSpecificElement<Component> = ComponentElements.empty()
        private set

    public fun setDisplay(header: PlayerSpecificElement<Component>, footer: PlayerSpecificElement<Component>) {
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

    public fun onPlayerJoin(player: ServerPlayer) {
        val actions = EnumSet.of(Action.UPDATE_LISTED)
        val entries = listOf(this.hidingClientboundEntry(player, true))
        this.sendToAllPlayers(ClientboundPlayerInfoUpdatePacket(actions, entries))
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.tabDisplay.set(this)

        this.resendTo(player, PlayerConnectionConsumer(player.connection))
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.tabDisplay.remove()

        val hiding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (other in player.server.playerList.players) {
            hiding.add(this.hidingClientboundEntry(other, false))
        }
        player.connection.send(ClientboundPlayerInfoUpdatePacket(EnumSet.of(Action.UPDATE_LISTED), hiding))

        val size = this.display.size
        val removing = ArrayList<UUID>(size)
        for (i in 0..< size) {
            removing.add(this.createUUIDForIndex(i))
        }
        player.connection.send(ClientboundPlayerInfoRemovePacket(removing))
    }

    override fun resendTo(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        if (sender !is PlayerConnectionConsumer) {
            player.tabDisplay.resend(sender)
        }

        val hiding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (other in player.server.playerList.players) {
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
                0,
                null
            ))
            this.sendToAllPlayers(ClientboundPlayerInfoUpdatePacket(actions, entries))
        }
        return null
    }

    private fun hidingClientboundEntry(player: ServerPlayer, hidden: Boolean): ClientboundPlayerInfoUpdatePacket.Entry {
        return ClientboundPlayerInfoUpdatePacket.Entry(
            player.uuid, null, !hidden, 0, GameType.SURVIVAL, null, 0, null
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
            0,
            null
        )
    }

    private fun createUUIDForIndex(index: Int): UUID {
        return UUID(index.toLong(), 0)
    }

    private fun createProfileForIndex(index: Int): GameProfile {
        return GameProfile(
            this.createUUIDForIndex(index),
            index.toString().padStart(2, '0')
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