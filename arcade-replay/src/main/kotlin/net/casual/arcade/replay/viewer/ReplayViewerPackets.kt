/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.viewer

import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket

public object ReplayViewerPackets {
    private val ALLOWED_SERVERBOUND: Set<Class<out Packet<*>>> = setOf(
        ServerboundKeepAlivePacket::class.java
    )
    private val ALLOWED_CLIENTBOUND: Set<Class<out Packet<*>>> = setOf(
        ClientboundKeepAlivePacket::class.java
    )

    @JvmStatic
    public fun clientboundBypass(packet: Packet<*>): Boolean {
        return ALLOWED_CLIENTBOUND.contains(packet::class.java)
    }

    @JvmStatic
    public fun serverboundBypass(packet: Packet<*>): Boolean {
        return ALLOWED_SERVERBOUND.contains(packet::class.java)
    }
}