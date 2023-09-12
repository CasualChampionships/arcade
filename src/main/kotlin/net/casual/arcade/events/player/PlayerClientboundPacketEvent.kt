package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

public data class PlayerClientboundPacketEvent(
    override val player: ServerPlayer,
    val packet: Packet<*>
): CancellableEvent.Typed<Packet<*>>(), PlayerEvent