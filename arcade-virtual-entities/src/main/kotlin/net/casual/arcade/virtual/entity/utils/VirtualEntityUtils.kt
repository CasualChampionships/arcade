/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.utils.math.location.Location
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.extensions.LevelAttachmentExtension.Companion.attachmentExtension
import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserver
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

public fun ServerLevel.addVirtualEntity(entity: VirtualEntity): Boolean {
    return this.attachmentExtension.attachment.attach(entity)
}

public fun ServerLevel.removeVirtualEntity(entity: VirtualEntity): Boolean {
    return this.attachmentExtension.attachment.detach(entity)
}

public fun ServerLevel.getVirtualEntities(): Collection<VirtualEntity> {
    return this.attachmentExtension.attachment.attached()
}

public fun VirtualEntity.location(): LocationWithLevel<ServerLevel> {
    val origin = this.attachment.origin
    val absolute = Location(this.position.get(origin.position), this.rotation.get(origin.rotation))
    return origin.copy(location = absolute)
}

public fun VirtualEntity.canAttachTo(attachment: VirtualEntityAttachment): Boolean {
    return this.attachment === attachment
}

public fun VirtualEntity.sendSpawnPackets(observer: ServerPlayer) {
    val collector = VirtualEntityPacketCollector()
    this.sendSpawnPackets(observer, collector::add)
    collector.optimize().bundle().send(observer.connection::send)
}

public fun VirtualEntity.sendDespawnPackets(observer: ServerPlayer) {
    val collector = VirtualEntityPacketCollector()
    this.sendDespawnPackets(observer, collector::add)
    collector.optimize().bundle().send(observer.connection::send)
}

public fun VirtualEntity.startObservingAndSendPackets(observer: ServerPlayer) {
    this.startObserving(observer)
    this.sendSpawnPackets(observer)
}

public fun VirtualEntity.stopObservingAndSendPackets(observer: ServerPlayer) {
    this.stopObserving(observer)
    this.sendDespawnPackets(observer)
}

public fun VirtualEntityAttachment.startObservingAttachedFor(observer: ServerPlayer) {
    observer.attachmentObserver.startObserving(this)

    val collector = VirtualEntityPacketCollector()
    for (entity in this.attached()) {
        entity.startObserving(observer)
        entity.sendSpawnPackets(observer, collector::add)
    }
    collector.optimize().bundle().send(observer.connection::send)
}

public fun VirtualEntityAttachment.stopObservingAttachedFor(observer: ServerPlayer) {
    observer.attachmentObserver.stopObserving(this)

    val collector = VirtualEntityPacketCollector()
    for (entity in this.attached()) {
        entity.startObserving(observer)
        entity.sendDespawnPackets(observer, collector::add)
    }
    collector.optimize().bundle().send(observer.connection::send)
}