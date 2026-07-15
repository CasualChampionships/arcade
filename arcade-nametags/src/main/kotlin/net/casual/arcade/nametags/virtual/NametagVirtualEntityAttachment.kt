/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import com.google.common.collect.Iterables
import com.google.common.collect.LinkedHashMultimap
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.nametagExtension
import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.networking.observer.tracker.ObserverTracker
import net.casual.arcade.networking.packet.PacketSender
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.EntityAttachmentAnchor
import net.casual.arcade.virtual.entity.utils.*
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.Entity
import net.casual.arcade.utils.ClientboundSetPassengersPacket as createSetPassengersPacket

public class NametagVirtualEntityAttachment(
    override val anchor: EntityAttachmentAnchor,
): RootVirtualEntityAttachment, ObserverTracker {
    private val nametags = Reference2ReferenceLinkedOpenHashMap<Nametag, NametagVirtualEntity>()
    private val tracked = LinkedHashMultimap.create<Observer, NametagVirtualEntity>()
    private val tracking = ReferenceLinkedOpenHashSet<Observer>()

    override val observers: ObserverTracker get() = this

    private val root = NametagHeightVirtualEntity(
        this, this.asParentObserverTracker(), NametagHeight.INITIAL, RetargetingInteractionHandler(this.entity)
    )

    private val entity: Entity
        get() = this.anchor.entity

    public fun attach(nametag: Nametag) {
        val entity = NametagVirtualEntity(this, this.entity, nametag)
        this.nametags[nametag] = entity

        entity.updateNametag()
    }

    public fun detach(nametag: Nametag) {
        val entity = this.nametags.remove(nametag) ?: return
        entity.observers.broadcast { observer ->
            entity.stopObservingAndSendPackets(observer)
            val tracked = this.tracked[observer]
            if (!tracked.isEmpty()) {
                tracked.remove(entity)
                this.resendNametagStackFor(observer, observer, tracked, false)
            }
        }
    }

    public fun detachAll() {
        for (entity in this.nametags.values) {
            entity.observers.broadcast { observer ->
                entity.stopObservingAndSendPackets(observer)
            }
        }
        this.tracked.clear()
        this.nametags.clear()
    }

    public fun sneak() {
        for (entity in this.nametags.values)  {
            entity.sneak()
        }
    }

    public fun unsneak() {
        for (entity in this.nametags.values)  {
            entity.unsneak()
        }
    }

    public fun getNametags(): Set<Nametag> {
        return this.nametags.keys
    }

    public fun getNametagEntities(): Collection<NametagVirtualEntity> {
        return this.nametags.values
    }

    public fun getRootId(): Int {
        return this.root.id
    }

    override fun tick() {
        this.observers.broadcast(this::updateObserver)

        super.tick()
    }
    override fun attach(entity: VirtualEntity): Boolean {
        return false
    }

    override fun detach(entity: VirtualEntity): Boolean {
        return false
    }

    override fun attached(): Iterable<VirtualEntity> {
        return Iterables.concat(this.nametags.values, listOf(this.root))
    }

    override fun observers(): Collection<Observer> {
        return this.tracking
    }

    override fun startObserving(observer: Observer): Boolean {
        return this.tracking.add(observer)
    }

    override fun stopObserving(observer: Observer) {
        this.tracking.remove(observer)
        this.tracked.removeAll(observer)
    }

    override fun isObserving(observer: Observer): Boolean {
        return this.tracking.contains(observer)
    }

    public fun isObservingEmpty(observer: Observer): Boolean {
        return this.tracked[observer].isEmpty()
    }

    override fun shouldDelayObserving(): Boolean {
        return true
    }

    override fun resendTo(observer: Observer, sender: PacketSender) {
        val tracked = this.tracked.get(observer)
        for (entity in tracked) {
            entity.sendBundledSpawnPackets(observer, sender)
        }

        this.resendNametagStackFor(observer, sender, tracked, true)
    }

    private fun updateObserver(observer: Observer) {
        val tracked = this.tracked.get(observer)
        val wasStackEmpty = tracked.isEmpty()
        var dirty = false
        for (entity in this.nametags.values) {
            val watching = entity.observers.isObserving(observer)
            val canWatch = entity.canObserve(observer)
            if (watching) {
                if (!canWatch) {
                    tracked.remove(entity)
                    entity.stopObservingAndSendPackets(observer)
                    dirty = true
                }
            } else if (canWatch) {
                tracked.add(entity)
                entity.startObservingAndSendPackets(observer)
                dirty = true
            }
        }

        if (dirty) {
            this.resendNametagStackFor(observer, observer, tracked, wasStackEmpty)
        }
    }

    private fun resendNametagStackFor(
        observer: Observer,
        sender: PacketSender,
        stack: Collection<NametagVirtualEntity>,
        wasStackEmpty: Boolean
    ) {
        if (stack.isEmpty()) {
            this.root.sendBundledDespawnPackets(observer, sender)
            return
        }
        if (wasStackEmpty) {
            this.root.sendBundledSpawnPackets(observer, sender)
            sender.send(this.entity.nametagExtension.createUpdatePassengersPacket(observer))
        }

        val entities = this.nametags.values.filter(stack::contains).reversed()
        if (entities.size >= 2) {
            // If we're the second last nametag then we need to ensure
            // that the previous vehicle is spawned in (see below)
            entities[entities.lastIndex - 1].getVehicle().sendBundledSpawnPackets(observer, sender)
        }

        var previous = this.root
        for (entity in entities) {
            sender.send(createSetPassengersPacket(previous.id, entity.getPassengerIds()))
            previous = entity.getVehicle()
        }

        // We remove the topmost height entity, we don't need it (nothing is mounted on it)
        sender.send(ClientboundRemoveEntitiesPacket(previous.id))
    }
}