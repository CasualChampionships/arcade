/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.extensions

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.entity.EntityTickEvent
import net.casual.arcade.extensions.EntityExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.event.EntityExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.utils.asClientGamePacket
import net.casual.arcade.utils.entity.EntityTransferReason
import net.casual.arcade.utils.getTrackingPlayers
import net.casual.arcade.utils.impl.DelayedActions
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.TrackingVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.EntityAttachmentAnchor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.function.Consumer

internal class EntityAttachmentExtension(entity: Entity): EntityExtension(entity) {
    private val attachments = lazy { ObjectLinkedOpenHashSet<RootVirtualEntityAttachment>() }
    private val anchor by lazy { EntityAttachmentAnchor(this.entity) }

    fun tick() {
        if (this.attachments.isInitialized()) {
            for (attachment in this.attachments.value) {
                attachment.tick()
            }
        }
    }

    fun <T: RootVirtualEntityAttachment> add(factory: (EntityAttachmentAnchor) -> T): T {
        val attachment = factory.invoke(this.anchor)
        require(attachment.anchor === this.anchor) { "Created VirtualEntityAttachment with incorrect anchor!" }
        this.attachments.value.add(attachment)
        for (player in this.entity.getTrackingPlayers()) {
            attachment.startObservingAttached(player)
        }
        return attachment
    }

    fun remove(attachment: RootVirtualEntityAttachment): Boolean {
        if (this.attachments.isInitialized() && this.attachments.value.remove(attachment)) {
            for (player in this.entity.getTrackingPlayers()) {
                attachment.stopObservingAttached(player)
            }
            return true
        }
        return false
    }

    fun getAttachedVirtualEntities(): List<VirtualEntity> {
        if (this.attachments.isInitialized()) {
            return this.attachments.value.flatMap { it.attached() }
        }
        return listOf()
    }

    fun startObserving(observer: ServerPlayer) {
        if (this.attachments.isInitialized()) {
            for (attachment in this.attachments.value) {
                attachment.startObservingAttached(observer, true)
            }
        }
    }

    fun sendObservingSpawnPackets(observer: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        if (this.attachments.isInitialized()) {
            for (attachment in this.attachments.value) {
                for (entity in attachment.attached()) {
                    entity.sendSpawnPackets(observer) { packet ->
                        consumer.accept(packet.asClientGamePacket())
                    }
                }
            }
        }
    }

    fun stopObserving(observer: ServerPlayer) {
        if (this.attachments.isInitialized()) {
            for (attachment in this.attachments.value) {
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
            GlobalEventHandler.Server.register<EntityExtensionEvent> {
                it.addExtension(::EntityAttachmentExtension)
            }
            GlobalEventHandler.Server.register<EntityTickEvent> { (entity) ->
                entity.attachmentExtension.tick()
            }
        }
    }
}