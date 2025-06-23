/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment
import eu.pb4.polymer.virtualentity.impl.HolderHolder
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.nametags.Nametag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Entity
import java.util.function.Consumer

public open class NametagElementHolder(
    private val entity: Entity
): ElementHolder() {
    private val nametags = Reference2ReferenceLinkedOpenHashMap<Nametag, NametagElement>()
    private val watching = Reference2ObjectLinkedOpenHashMap<ServerGamePacketListenerImpl, MutableSet<NametagElement>>()

    public val root: NametagHeightElement = NametagHeightElement(NametagHeight.INITIAL)

    init {
        this.addElement(this.root)
    }

    public fun add(nametag: Nametag) {
        val element = NametagElement(this.entity, nametag)
        this.nametags[nametag] = element

        // Manually call the first update
        element.update()

        this.addElementWithoutUpdates(element)
        this.onAddNametag(nametag, element)
    }

    public fun remove(nametag: Nametag) {
        val element = this.nametags.remove(nametag) ?: return

        for (connection in element.getObservers()) {
            element.sendRemovePackets(connection.player, connection::send)
            val watching = this.watching[connection] ?: continue
            watching.remove(element)
            this.resendNametagStackFor(watching, connection, connection::send)
        }

        this.removeElementWithoutUpdates(element)
        this.onRemoveNametag(nametag, element)
    }

    public fun removeAll() {
        for (element in this.nametags.values) {
            for (connection in element.getObservers()) {
                element.sendRemovePackets(connection.player, connection::send)
            }
        }
        this.watching.clear()
        this.nametags.clear()
        this.onRemoveAllNametags()
    }

    public fun sneak() {
        for (element in this.nametags.values) {
            element.sneak()
        }
    }

    public fun unsneak() {
        for (element in this.nametags.values) {
            element.unsneak()
        }
    }

    public fun getNametagElements(): Collection<NametagElement> {
        return this.nametags.values
    }

    public fun isWatching(player: ServerPlayer): Boolean {
        return this.watching.containsKey(player.connection)
    }

    override fun onTick() {
        for (observer in this.watchingPlayers) {
            this.updateObserver(observer, observer::send)
        }
    }

    override fun startWatching(connection: ServerGamePacketListenerImpl?): Boolean {
        if (this.watchingPlayers.contains(connection)) {
            return false
        }

        this.watchingPlayers.add(connection)
        (connection as HolderHolder).`polymer$addHolder`(this)
        val packets = ObjectArrayList<Packet<in ClientGamePacketListener>>()

        this.root.startWatching(connection.player, packets::add)

        this.startWatchingExtraPackets(connection, packets::add)

        this.attachment?.startWatchingExtraPackets(connection, packets::add)

        connection.send(ClientboundBundlePacket(packets))
        return true
    }

    override fun startWatchingExtraPackets(
        connection: ServerGamePacketListenerImpl,
        consumer: Consumer<Packet<ClientGamePacketListener>>
    ) {
        this.updateObserver(connection, consumer)
    }

    override fun stopWatching(connection: ServerGamePacketListenerImpl): Boolean {
        if (super.stopWatching(connection)) {
            this.watching.remove(connection)
            return true
        }
        return false
    }

    override fun onAttachmentRemoved(old: HolderAttachment) {
        this.nametags.clear()
        this.watching.clear()
    }

    public open fun isMountedToOwner(): Boolean {
        return true
    }

    protected open fun onAddNametag(nametag: Nametag, element: NametagElement) {

    }

    protected open fun onRemoveNametag(nametag: Nametag, element: NametagElement) {

    }

    protected open fun onRemoveAllNametags() {

    }

    protected open fun updateObserver(
        connection: ServerGamePacketListenerImpl,
        consumer: Consumer<Packet<ClientGamePacketListener>>
    ) {
        val elements = this.watching.getOrPut(connection, ::ReferenceLinkedOpenHashSet)

        var dirty = false
        for ((nametag, element) in this.nametags) {
            val watching = element.getObservers().contains(connection)

            val canWatch = this.entity.broadcastToPlayer(connection.player) &&
                    nametag.isObservable(this.entity, connection.player) &&
                    nametag.isWithinRange(this.entity, connection.player)

            if (watching) {
                if (!canWatch) {
                    elements.remove(element)
                    element.stopWatching(connection.player, consumer)
                    consumer.accept(ClientboundRemoveEntitiesPacket(element.entityIds))
                    dirty = true
                }
            } else if (canWatch) {
                elements.add(element)
                element.startWatching(connection.player, consumer)
                dirty = true
            }
        }

        if (dirty) {
            this.resendNametagStackFor(elements, connection, consumer)
        }
    }

    protected open fun resendNametagStackFor(
        stack: Collection<NametagElement>,
        connection: ServerGamePacketListenerImpl,
        consumer: Consumer<Packet<ClientGamePacketListener>>
    ) {
        if (stack.isEmpty()) {
            return
        }

        var previous = this.root.id
        for (element in this.nametags.values.reversed()) {
            if (!stack.contains(element)) {
                continue
            }

            val packet = VirtualEntityUtils.createRidePacket(previous, element.entityIds)
            consumer.accept(packet)

            previous = element.getMountingId()
        }

        if (this.isMountedToOwner()) {
            consumer.accept(ClientboundSetPassengersPacket(this.entity))
        }
    }
}