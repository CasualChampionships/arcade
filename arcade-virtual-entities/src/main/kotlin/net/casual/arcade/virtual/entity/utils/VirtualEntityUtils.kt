/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.EntityAttachmentAnchor
import net.casual.arcade.virtual.entity.attachment.anchor.LevelAttachmentAnchor
import net.casual.arcade.virtual.entity.extensions.EntityAttachmentExtension.Companion.attachmentExtension
import net.casual.arcade.virtual.entity.extensions.LevelAttachmentExtension.Companion.attachmentExtension
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.tracker.ParentObserverTracker
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public fun <T: RootVirtualEntityAttachment> ServerLevel.createVirtualEntityAttachment(factory: (LevelAttachmentAnchor) -> T): T {
    return this.attachmentExtension.add(factory)
}

public fun ServerLevel.removeVirtualEntityAttachment(attachment: RootVirtualEntityAttachment): Boolean {
    return this.attachmentExtension.remove(attachment)
}

public fun ServerLevel.getVirtualEntities(): Collection<VirtualEntity> {
    return this.attachmentExtension.getAttachedVirtualEntities()
}

public fun <T: RootVirtualEntityAttachment> Entity.createVirtualEntityAttachment(factory: (EntityAttachmentAnchor) -> T): T {
    return this.attachmentExtension.add(factory)
}

public fun Entity.removeVirtualEntityAttachment(attachment: RootVirtualEntityAttachment): Boolean {
    return this.attachmentExtension.remove(attachment)
}

public fun Entity.getVirtualEntities(): Collection<VirtualEntity> {
    return this.attachmentExtension.getAttachedVirtualEntities()
}

public fun VirtualEntity.location(): Location {
    val origin = this.attachment.anchor.location()
    return Location(this.position.get(origin.position), this.rotation.get(origin.rotation))
}

public fun VirtualEntity.canAttachTo(attachment: VirtualEntityAttachment): Boolean {
    return this.attachment === attachment
}

public fun VirtualEntity.sendSpawnPackets(
    observer: ServerPlayer,
    consumer: (Packet<*>) -> Unit = observer.connection::send
) {
    val collector = VirtualEntityPacketCollector()
    this.sendSpawnPackets(observer, collector::add)
    collector.bundle().send(consumer)
}

public fun VirtualEntity.sendDespawnPackets(
    observer: ServerPlayer,
    consumer: (Packet<*>) -> Unit = observer.connection::send
) {
    val collector = VirtualEntityPacketCollector()
    this.sendDespawnPackets(observer, collector::add)
    collector.optimize().bundle().send(consumer)
}

public fun VirtualEntity.startObservingAndSendPackets(
    observer: ServerPlayer,
    consumer: (Packet<*>) -> Unit = observer.connection::send
) {
    if (this.canObserve(observer) && this.observers.startObserving(observer)) {
        this.sendSpawnPackets(observer, consumer)
    }
}

public fun VirtualEntity.stopObservingAndSendPackets(
    observer: ServerPlayer,
    consumer: (Packet<*>) -> Unit = observer.connection::send
) {
    if (this.observers.isObserving(observer)) {
        this.sendDespawnPackets(observer, consumer)
        this.observers.stopObserving(observer)
    }
}

public fun VirtualEntityAttachment.createParentObserverTracker(): ParentObserverTracker {
    return this.observers as? ParentObserverTracker ?: ParentObserverTracker(this.observers)
}

public inline fun <A: VirtualEntityAttachment, T: VirtualEntity> A.attach(factory: (A) -> T): T {
    val entity = factory.invoke(this)
    this.attach(entity)
    return entity
}

public inline fun <A: VirtualEntityAttachment, T: VirtualEntity> A.attachWithParentObservers(
    factory: (A, ObserverTracker) -> T
): T {
    val entity = factory.invoke(this, this.createParentObserverTracker())
    this.attach(entity)
    return entity
}