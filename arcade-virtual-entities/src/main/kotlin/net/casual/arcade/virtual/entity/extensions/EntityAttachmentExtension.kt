/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.entity.EntityTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.utils.asClientGamePacket
import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.impl.DelayedActions
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.EntityAttachmentAnchor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.world.entity.Entity
import java.util.function.Consumer

internal class EntityAttachmentExtension(entity: Entity): EntityExtension(entity) {
    private val attachments = ArrayList<RootVirtualEntityAttachment>(4)
    private val observers = ObjectLinkedOpenHashSet<Observer>()
    private val anchor = EntityAttachmentAnchor(this.entity)

    private fun tick() {
        for (attachment in this.attachments) {
            attachment.tick()
        }
    }

    fun <T: RootVirtualEntityAttachment> add(factory: (EntityAttachmentAnchor) -> T): T {
        val attachment = factory.invoke(this.anchor)
        require(attachment.anchor === this.anchor) { "Created VirtualEntityAttachment with incorrect anchor!" }
        require(!this.attachments.contains(attachment)) { "Created VirtualEntityAttachment was already attached!" }
        this.attachments.add(attachment)
        for (observer in this.observers) {
            attachment.startObservingAttached(observer)
        }
        return attachment
    }

    fun remove(attachment: RootVirtualEntityAttachment): Boolean {
        if (this.attachments.remove(attachment)) {
            attachment.clearObservingAttached()
            return true
        }
        return false
    }

    fun getAttachments(): Collection<RootVirtualEntityAttachment> {
        return this.attachments
    }

    fun getAttachedVirtualEntities(): List<VirtualEntity> {
        if (this.attachments.isEmpty()) {
            return listOf()
        }
        return this.attachments.flatMap { it.attached() }
    }

    fun startObserving(observer: Observer) {
        if (this.observers.add(observer)) {
            for (attachment in this.attachments) {
                attachment.startObservingAttached(observer, true)
            }
        }
    }

    fun sendObservingSpawnPackets(observer: Observer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        for (attachment in this.attachments) {
            attachment.sendObservingAttachedSpawnPackets(observer) { packet ->
                consumer.accept(packet.asClientGamePacket())
            }
        }
    }

    fun stopObserving(observer: Observer) {
        if (this.observers.remove(observer)) {
            for (attachment in this.attachments) {
                attachment.stopObservingAttached(observer)
            }
        }
    }

    override fun transfer(
        entity: Entity,
        reason: EntityTransferReason,
        delayed: DelayedActions
    ): Extension {
        return EntityAttachmentExtension(entity)
    }

    companion object {
        @JvmStatic
        val Entity.attachmentExtension: EntityAttachmentExtension
            get() = this.getExtension()

        fun registerEvents() {
            GlobalEventHandler.Server.register<EntityExtensionEvent>(priority = 3) {
                it.addExtension(::EntityAttachmentExtension)
            }
            GlobalEventHandler.Server.register<EntityTickEvent> { (entity) ->
                entity.attachmentExtension.tick()
            }
        }
    }
}