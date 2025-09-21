/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.casual.arcade.utils.modify
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.server.level.ServerPlayer

public data class PlayerClientboundPacketEvent(
    override val player: ServerPlayer,
    var packet: Packet<*>
): CancellableEvent.Default(), PlayerEvent, AsyncEvent {
    public companion object {
        /**
         * The phase is invoked before the packet has been sent to the player.
         * The [packet] can be modified in this phase.
         *
         * This is the default phase for this event.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        /**
         * This phase in invoked **after** the packet has been sent to the player.
         * The [packet] can no longer be modified.
         */
        public const val PHASE_POST: String = BuiltInEventPhases.POST

        public inline fun PlayerClientboundPacketEvent.replacePacket(
            replacement: (ServerPlayer, Packet<*>) -> Packet<*>?
        ) {
            if (!this.isCancelled()) {
                val packet = replacement.invoke(this.player, this.packet)
                if (packet == null) {
                    this.cancel()
                } else {
                    this.packet = packet
                }
            }
        }

        public inline fun PlayerClientboundPacketEvent.replacePacketRecursively(
            replacement: (ServerPlayer, Packet<*>) -> Packet<*>?
        ) {
            this.replacePacket { player, packet ->
                when (packet) {
                    is ClientboundBundlePacket -> packet.modify(player, replacement)
                    else -> replacement.invoke(player, packet)
                }
            }
        }
    }
}