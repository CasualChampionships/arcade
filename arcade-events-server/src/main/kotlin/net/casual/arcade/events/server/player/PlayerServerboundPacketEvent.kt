/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

public data class PlayerServerboundPacketEvent(
    override val player: ServerPlayer,
    val packet: Packet<*>
): CancellableEvent.Default(), PlayerEvent, AsyncEvent