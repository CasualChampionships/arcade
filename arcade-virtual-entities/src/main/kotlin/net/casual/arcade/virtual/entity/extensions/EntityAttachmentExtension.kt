/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.entity.EntityTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.events.ObserverStartObservingEntityEvent
import net.casual.arcade.observer.events.ObserverStopObservingEntityEvent
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.utils.getObservers
import net.casual.arcade.utils.asClientGamePacket
import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.impl.DelayedActions
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.EntityAttachmentAnchor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.world.entity.Entity
import java.util.function.Consumer

internal class EntityAttachmentExtension(
    entity: Entity
): EntityExtension(entity), AttachmentExtension<EntityAttachmentAnchor> {
    override val attachments = ArrayList<RootVirtualEntityAttachment>(4)
    override val anchor = EntityAttachmentAnchor(this.entity)

    override fun getObservers(): ObserverTracker {
        return this.entity.getObservers()
    }

    fun sendObservingSpawnPackets(observer: Observer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        for (attachment in this.attachments) {
            attachment.sendObservingAttachedSpawnPackets(observer) { packet ->
                consumer.accept(packet.asClientGamePacket())
            }
        }
    }

    private fun startObserving(observer: Observer) {
        for (attachment in this.attachments) {
            attachment.startObservingAttached(observer, true)
        }
    }

    private fun stopObserving(observer: Observer) {
        for (attachment in this.attachments) {
            attachment.stopObservingAttached(observer)
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
            GlobalEventHandler.Server.register<ObserverStartObservingEntityEvent> { (observer, entity) ->
                entity.attachmentExtension.startObserving(observer)
            }
            GlobalEventHandler.Server.register<ObserverStopObservingEntityEvent> { (observer, entity) ->
                entity.attachmentExtension.stopObserving(observer)
            }
        }
    }
}