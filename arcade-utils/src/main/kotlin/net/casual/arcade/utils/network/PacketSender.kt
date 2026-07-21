/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.network

import net.minecraft.network.protocol.Packet

public fun interface PacketSender {
    public fun send(packet: Packet<*>)
}