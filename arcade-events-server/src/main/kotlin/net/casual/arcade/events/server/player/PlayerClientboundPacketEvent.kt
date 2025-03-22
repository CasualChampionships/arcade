/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.common.MissingExecutorEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

public data class PlayerClientboundPacketEvent(
    override val player: ServerPlayer,
    var packet: Packet<*>
): CancellableEvent.Default(), PlayerEvent, MissingExecutorEvent {
    public companion object {
        /**
         * The phase is invoked before the packet has been sent to the player.
         * The [packet] can be modified in this phase.
         *
         * This is the default phase for this event.
         */
        public const val PRE_PHASE: String = BuiltInEventPhases.PRE

        /**
         * This phase in invoked **after** the packet has been sent to the player.
         * The [packet] can no longer be modified.
         */
        public const val POST_PHASE: String = BuiltInEventPhases.POST
    }
}