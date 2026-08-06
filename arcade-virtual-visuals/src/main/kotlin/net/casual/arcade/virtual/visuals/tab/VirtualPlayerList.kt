/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.tab

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.utils.ClientboundPlayerInfoUpdatePacket
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.virtual.visuals.VirtualVisual
import net.casual.arcade.virtual.visuals.data.PlayerSpecificValue
import net.casual.arcade.virtual.visuals.data.PlayerSpecificVisualData
import net.casual.arcade.virtual.visuals.tab.PlayerListEntries.Entry
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import java.util.*

/**
 * A player list implementation of [VirtualVisual].
 *
 * The [header] and [footer] have a base value, which all observers
 * are shown by default, as well as optional per-player overrides.
 * The [entries] displayed in the list are shared by all observers:
 * ```
 * val list = VirtualPlayerList(server, VanillaPlayerListEntries())
 * list.header.set(Component.literal("Welcome!"))
 * list.footer.set(player, Component.literal("Welcome, ${player.username}!"))
 * ```
 *
 * An observer can only be shown one player list at a time; observing
 * this list will stop them observing whichever list they were shown
 * before.
 *
 * @param server The server this player list belongs to.
 * @param entries The entries to display in the list.
 * @param observers The observer tracker for this player list.
 * @see PlayerSpecificValue
 */
public open class VirtualPlayerList(
    /**
     * The server this player list belongs to.
     */
    protected val server: MinecraftServer,
    /**
     * The entries displayed in the list.
     */
    public var entries: PlayerListEntries,
    override val observers: ObserverTracker = SimpleObserverTracker()
): VirtualVisual {
    private val previous = ArrayList<Entry>()

    /**
     * The data for this player list.
     */
    protected val data: PlayerSpecificVisualData = PlayerSpecificVisualData()

    /**
     * The header displayed above the list.
     */
    public val header: PlayerSpecificValue<Component> = this.data.register(CommonComponents.EMPTY)

    /**
     * The footer displayed below the list.
     */
    public val footer: PlayerSpecificValue<Component> = this.data.register(CommonComponents.EMPTY)

    private val display = this.header.bit or this.footer.bit

    override fun tick() {
        this.entries.tick(this.server)
        this.tickEntries()

        val base = this.data.clean()
        this.observers.broadcast { observer ->
            this.sendDirtyPackets(observer, base)
        }
    }

    override fun shouldTickWhenPaused(): Boolean {
        return true
    }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        sender.send(ClientboundTabListPacket(this.header.get(observer), this.footer.get(observer)))

        val hiding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (player in this.server.playerList.players) {
            hiding.add(this.createHidingEntry(player, true))
        }
        sender.send(ClientboundPlayerInfoUpdatePacket(EnumSet.of(Action.UPDATE_LISTED), hiding))

        val adding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (index in 0..< this.entries.size) {
            adding.add(this.createClientboundEntry(index, this.entries.getEntryAt(index)))
        }
        sender.send(ClientboundPlayerInfoUpdatePacket(EnumUtils.completeSet(), adding))
    }

    override fun sendDespawnPackets(observer: Observer, sender: PacketSender) {
        val hiding = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        for (player in this.server.playerList.players) {
            hiding.add(this.createHidingEntry(player, false))
        }
        sender.send(ClientboundPlayerInfoUpdatePacket(EnumSet.of(Action.UPDATE_LISTED), hiding))

        val size = this.entries.size
        val removing = ArrayList<UUID>(size)
        for (index in 0..< size) {
            removing.add(this.createUUIDForIndex(index))
        }
        sender.send(ClientboundPlayerInfoRemovePacket(removing))
        sender.send(ClientboundTabListPacket(CommonComponents.EMPTY, CommonComponents.EMPTY))
    }

    override fun onStartObserving(observer: Observer) {
        val player = observer.asPlayerOrNull()
        if (player !== null) {
            this.data.clean(player.uuid)
        }

        val current = observer.context.get(CURRENT_PLAYER_LIST)
        if (current !== null && current !== this) {
            current.stopObservingAndSendPackets(observer)
        }
        observer.context.set(CURRENT_PLAYER_LIST, this)
    }

    override fun onStopObserving(observer: Observer) {
        if (observer.context.get(CURRENT_PLAYER_LIST) === this) {
            observer.context.remove(CURRENT_PLAYER_LIST)
        }
    }

    /**
     * Rewrites a [ClientboundPlayerInfoUpdatePacket] being sent to the
     * given [receiver] so that real players are never listed, leaving
     * only this list's [entries] visible.
     *
     * @param receiver The player being sent the packet.
     * @param packet The packet being sent.
     * @return The packet to send instead.
     */
    public open fun replacePlayerInfoUpdatePacket(
        receiver: ServerPlayer,
        packet: ClientboundPlayerInfoUpdatePacket
    ): ClientboundPlayerInfoUpdatePacket {
        if (!this.observers.isObserving(receiver.asObserver())) {
            return packet
        }
        if (!packet.actions().contains(Action.UPDATE_LISTED)) {
            return packet
        }

        val mapped = packet.entries().map { entry ->
            if (this.isIndexUUID(entry.profileId)) entry else ClientboundPlayerInfoUpdatePacket.Entry(
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
        }
        return ClientboundPlayerInfoUpdatePacket(packet.actions(), mapped)
    }

    /**
     * Sends the given [observer] the packets for any of this list's
     * values which have changed for them since the last tick.
     *
     * @param observer The observer to send packets to.
     * @param baseDirty The mask returned by [PlayerSpecificVisualData.clean].
     */
    protected open fun sendDirtyPackets(observer: Observer, baseDirty: Int) {
        val player = observer.asPlayerOrNull()
        val dirty = if (player != null) this.data.clean(player.uuid, baseDirty) else baseDirty
        if (dirty and this.display != 0) {
            observer.send(ClientboundTabListPacket(this.header.get(observer), this.footer.get(observer)))
        }
    }

    private fun tickEntries() {
        // We try to be as efficient as possible with these packets
        val removing = ClientboundPlayerInfoRemovePacket(ArrayList())
        val entries = ArrayList<ClientboundPlayerInfoUpdatePacket.Entry>()
        val uuids = removing.profileIds

        val size = this.entries.size
        val previousSize = this.previous.size
        if (size > previousSize) {
            for (unused in previousSize..< size) {
                this.previous.add(Entry.DEFAULT)
            }
        } else if (size < previousSize) {
            this.previous.subList(size, previousSize).clear()
            for (index in size..< previousSize) {
                uuids.add(this.createUUIDForIndex(index))
            }
        }

        for (index in 0..< size) {
            val entry = this.checkEntryUpdate(index, index >= previousSize) ?: continue
            val clientbound = this.createClientboundEntry(index, entry)
            entries.add(clientbound)
            if (index < previousSize) {
                uuids.add(clientbound.profileId)
            }
        }

        if (uuids.isNotEmpty()) {
            this.observers.broadcast(removing)
        }
        if (entries.isNotEmpty()) {
            this.observers.broadcast(ClientboundPlayerInfoUpdatePacket(EnumUtils.completeSet(), entries))
        }
    }

    private fun checkEntryUpdate(index: Int, added: Boolean): Entry? {
        val entry = this.entries.getEntryAt(index)
        val previous = this.previous[index]
        this.previous[index] = entry

        if (entry.textures != previous.textures || added) {
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
                if (entry.spectatorFormat) GameType.SPECTATOR else GameType.SURVIVAL,
                entry.display,
                entry.showHat,
                -index, // For some reason, Mojang does this in reverse
                null
            ))
            this.observers.broadcast(ClientboundPlayerInfoUpdatePacket(actions, entries))
        }
        return null
    }

    private fun createHidingEntry(player: ServerPlayer, hidden: Boolean): ClientboundPlayerInfoUpdatePacket.Entry {
        return ClientboundPlayerInfoUpdatePacket.Entry(
            player.uuid, null, !hidden, 0, GameType.SURVIVAL, null, true, 0, null
        )
    }

    private fun createClientboundEntry(index: Int, entry: Entry): ClientboundPlayerInfoUpdatePacket.Entry {
        val properties = ImmutableMultimap.of("textures", entry.textures.toProperty())
        val profile = this.createProfileForIndex(index, PropertyMap(properties))
        return ClientboundPlayerInfoUpdatePacket.Entry(
            profile.id,
            profile,
            true,
            entry.latency,
            if (entry.spectatorFormat) GameType.SPECTATOR else GameType.SURVIVAL,
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

    private fun createProfileForIndex(index: Int, properties: PropertyMap = PropertyMap.EMPTY): GameProfile {
        val char = (0x00B4 + index).toChar()
        return GameProfile(this.createUUIDForIndex(index), char.toString(), properties)
    }

    private companion object {
        private val CURRENT_PLAYER_LIST = Observer.Context.Key<VirtualPlayerList>(arcade("virtual_player_list"))
    }
}
