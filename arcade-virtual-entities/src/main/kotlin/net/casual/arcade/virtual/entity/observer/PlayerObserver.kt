/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.observer

import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.casual.arcade.virtual.entity.extensions.PlayerAttachmentObserverExtension.Companion.attachmentObserverExtension
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.phys.Vec3

public class PlayerObserver internal constructor(
    private val connection: ServerGamePacketListenerImpl
): Observer {
    public val player: ServerPlayer
        get() = this.connection.player

    override fun position(): Vec3 {
        return this.player.position()
    }

    override fun send(packet: Packet<*>) {
        this.connection.send(packet)
    }

    override fun startObserving(attachment: RootVirtualEntityAttachment) {
        this.player.attachmentObserverExtension.startObserving(attachment)
    }

    override fun stopObserving(attachment: RootVirtualEntityAttachment) {
        this.player.attachmentObserverExtension.stopObserving(attachment)
    }

    override fun hashCode(): Int {
        return this.connection.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is PlayerObserver && this.connection == other.connection)
    }
}