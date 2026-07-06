/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.client.network

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.Packet

public data class ServerboundPacketEvent(
    val minecraft: Minecraft,
    var packet: Packet<*>
): CancellableEvent.Default(), AsyncEvent {
    public companion object {
        /**
         * The phase is invoked before the packet has been sent to the server.
         * The [packet] can be modified in this phase.
         *
         * This is the default phase for this event.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        /**
         * This phase in invoked **after** the packet has been sent to the server.
         * The [packet] can no longer be modified.
         */
        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}