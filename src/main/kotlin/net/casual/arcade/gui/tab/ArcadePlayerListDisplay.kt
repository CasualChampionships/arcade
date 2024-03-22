package net.casual.arcade.gui.tab

import com.mojang.authlib.GameProfile
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils
import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.TickableUI
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.gui.tab.PlayerListEntries.Entry
import net.casual.arcade.utils.TabUtils.tabDisplay
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.GameType
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

public class ArcadePlayerListDisplay(
    private val display: PlayerListEntries
): PlayerUI(), TickableUI {
    private val previous = ArrayList<Entry>()

    public var header: ComponentSupplier = ComponentSupplier.empty()
        private set
    public var footer: ComponentSupplier = ComponentSupplier.empty()
        private set

    public fun setDisplay(header: ComponentSupplier, footer: ComponentSupplier) {
        this.header = header
        this.footer = footer

        for (player in this.getPlayers()) {
            player.tabDisplay.setDisplay(header.getComponent(player), footer.getComponent(player))
        }
    }

    public override fun tick() {
        // We try to be as efficient as possible with these packets
        val adding = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.allOf(Action::class.java))
        val removing = ClientboundPlayerInfoRemovePacket(ArrayList())
        val entries = adding.entries()
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
            if (i <= previousSize) {
                uuids.add(clientbound.profileId)
            }
        }

        if (uuids.isNotEmpty()) {
            this.sendToAllPlayers(removing)
        }
        if (entries.isNotEmpty()) {
            this.sendToAllPlayers(adding)
        }
    }

    public fun onPlayerJoin(player: ServerPlayer) {
        val hiding = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(Action.UPDATE_LISTED))
        hiding.entries().add(this.hidingClientboundEntry(player, true))
        this.sendToAllPlayers(hiding)
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.tabDisplay.set(this)

        this.resendTo(player, PlayerConnectionConsumer(player.connection))
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.tabDisplay.remove()

        val hiding = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(Action.UPDATE_LISTED))
        for (other in player.server.playerList.players) {
            hiding.entries().add(this.hidingClientboundEntry(other, false))
        }
        player.connection.send(hiding)

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

        val hiding = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.of(Action.UPDATE_LISTED))
        for (other in player.server.playerList.players) {
            hiding.entries().add(this.hidingClientboundEntry(other, true))
        }
        sender.accept(hiding)

        val adding = PolymerEntityUtils.createMutablePlayerListPacket(EnumSet.allOf(Action::class.java))
        val entries = adding.entries()
        for (i in 0..< this.display.size) {
            val entry = this.display.getEntryAt(i)
            entries.add(this.toClientboundEntry(i, entry))
        }
        sender.accept(adding)
    }

    private fun checkEntryUpdate(index: Int): Entry? {
        val entry = this.display.getEntryAt(index)
        val previous = this.previous[index]
        this.previous[index] = entry

        if (entry.textures != previous.textures) {
            // The entire entry needs to be resent
            return entry
        }

        val actions = EnumSet.noneOf(Action::class.java)
        if (entry.latency != previous.latency) {
            actions.add(Action.UPDATE_LATENCY)
        }
        if (entry.display != previous.display) {
            actions.add(Action.UPDATE_DISPLAY_NAME)
        }

        if (actions.isNotEmpty()) {
            val packet = PolymerEntityUtils.createMutablePlayerListPacket(actions)
            packet.entries().add(ClientboundPlayerInfoUpdatePacket.Entry(
                this.createUUIDForIndex(index),
                // We don't need to calculate the GP
                null,
                true,
                entry.latency,
                GameType.SURVIVAL,
                entry.display,
                null
            ))
        }
        return null
    }

    private fun hidingClientboundEntry(player: ServerPlayer, hidden: Boolean): ClientboundPlayerInfoUpdatePacket.Entry {
        return ClientboundPlayerInfoUpdatePacket.Entry(
            player.uuid, null, !hidden, 0, GameType.SURVIVAL, null, null
        )
    }

    private fun toClientboundEntry(index: Int, entry: Entry): ClientboundPlayerInfoUpdatePacket.Entry {
        val profile = this.createProfileForIndex(index)
        return ClientboundPlayerInfoUpdatePacket.Entry(
            profile.id,
            profile,
            true,
            entry.latency,
            GameType.SURVIVAL,
            entry.display,
            null
        )
    }

    private fun createUUIDForIndex(index: Int): UUID {
        return UUID(index.toLong(), 0)
    }

    private fun createProfileForIndex(index: Int): GameProfile {
        return GameProfile(
            this.createUUIDForIndex(index),
            index.toString()
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