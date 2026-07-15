/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.networking.observer

import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.locationWithLevel
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

public class PlayerObserver internal constructor(
    private val connection: ServerGamePacketListenerImpl
): Observer {
    public val player: ServerPlayer
        get() = this.connection.player

    override val location: LocationWithLevel<ServerLevel>
        get() = this.player.locationWithLevel

    override fun send(packet: Packet<*>) {
        this.connection.send(packet)
    }

    override fun hashCode(): Int {
        return this.connection.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is PlayerObserver && this.connection == other.connection)
    }
}