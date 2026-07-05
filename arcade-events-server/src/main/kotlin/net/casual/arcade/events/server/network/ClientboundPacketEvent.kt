/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.network

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer

public data class ClientboundPacketEvent(
    val server: MinecraftServer,
    val owner: GameProfile,
    var packet: Packet<*>
): CancellableEvent.Default(), AsyncEvent {
    public companion object {
        /**
         * The phase is invoked before the packet has been sent to the client.
         * The [packet] can be modified in this phase.
         *
         * This is the default phase for this event.
         */
        public const val PHASE_PRE: Int = BuiltInEventPhases.PRE

        /**
         * This phase in invoked **after** the packet has been sent to the client.
         * The [packet] can no longer be modified.
         */
        public const val PHASE_POST: Int = BuiltInEventPhases.POST
    }
}