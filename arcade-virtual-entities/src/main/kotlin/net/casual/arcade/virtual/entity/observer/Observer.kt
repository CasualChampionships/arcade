/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.observer

import net.casual.arcade.virtual.entity.attachment.RootVirtualEntityAttachment
import net.minecraft.network.protocol.Packet
import net.minecraft.world.phys.Vec3
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface Observer: PacketSender {
    public fun position(): Vec3

    public override fun send(packet: Packet<*>)

    public override fun hashCode(): Int

    public override operator fun equals(other: Any?): Boolean

    @OverrideOnly
    public fun startObserving(attachment: RootVirtualEntityAttachment) {

    }

    @OverrideOnly
    public fun stopObserving(attachment: RootVirtualEntityAttachment) {

    }
}