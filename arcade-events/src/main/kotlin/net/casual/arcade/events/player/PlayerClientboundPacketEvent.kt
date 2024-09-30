package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.server.ServerOffThreadEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

public data class PlayerClientboundPacketEvent(
    override val player: ServerPlayer,
    var packet: Packet<*>
): CancellableEvent.Default(), PlayerEvent, ServerOffThreadEvent