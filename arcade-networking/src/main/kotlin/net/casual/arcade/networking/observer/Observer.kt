/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.networking.observer

import net.casual.arcade.networking.packet.PacketSender
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerLevel

public interface Observer: PacketSender {
    public val location: LocationWithLevel<ServerLevel>

    public override fun send(packet: Packet<*>)

    public override fun hashCode(): Int

    public override operator fun equals(other: Any?): Boolean
}