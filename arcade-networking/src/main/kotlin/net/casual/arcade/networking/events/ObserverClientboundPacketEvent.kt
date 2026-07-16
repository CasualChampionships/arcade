package net.casual.arcade.networking.events

import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.utils.modify
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundBundlePacket

public data class ObserverClientboundPacketEvent(
    public val observer: Observer,
    var packet: Packet<*>
): ServerSideEvent, AsyncEvent {
    public companion object {
        public inline fun ObserverClientboundPacketEvent.replacePacket(
            replacement: (Observer, Packet<*>) -> Packet<*>
        ) {
            this.packet = replacement.invoke(this.observer, this.packet)
        }

        public inline fun ObserverClientboundPacketEvent.replacePacketRecursively(
            replacement: (Observer, Packet<*>) -> Packet<*>
        ) {
            this.replacePacket { observer, packet ->
                when (packet) {
                    is ClientboundBundlePacket -> packet.modify(observer, replacement)
                    else -> replacement.invoke(observer, packet)
                }
            }
        }
    }
}