package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

class PlayerClientboundPacketEvent(
    val player: ServerPlayer,
    val packet: Packet<*>
): CancellableEvent.Typed<Packet<*>>()