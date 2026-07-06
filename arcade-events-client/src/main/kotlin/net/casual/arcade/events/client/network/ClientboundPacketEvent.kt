/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.network

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.Packet

public data class ClientboundPacketEvent(
    val minecraft: Minecraft,
    val packet: Packet<*>
): CancellableEvent.Default(), AsyncEvent