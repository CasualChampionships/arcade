package net.casual.arcade.virtual.entity

import net.casual.arcade.utils.math.location.Location
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer

public interface ParentVirtualEntity: VirtualEntity {
    public val canInteractWithChildren: Boolean
        get() = false

    public fun children(): Collection<VirtualEntity>

    override fun tick(attachment: VirtualEntityAttachment) {
        for (child in this.children()) {
            child.tick(attachment)
        }
    }

    override fun startObserving(observer: ServerPlayer) {
        for (child in this.children()) {
            child.startObserving(observer)
        }
    }

    override fun stopObserving(observer: ServerPlayer) {
        for (child in this.children()) {
            child.stopObserving(observer)
        }
    }

    override fun sendSpawnPackets(observer: ServerPlayer, origin: Location, consumer: (Packet<*>) -> Unit) {
        for (child in this.children()) {
            child.sendSpawnPackets(observer, origin, consumer)
        }
    }

    override fun sendDespawnPackets(observer: ServerPlayer, origin: Location, consumer: (Packet<*>) -> Unit) {
        for (child in this.children()) {
            child.sendDespawnPackets(observer, origin, consumer)
        }
    }
}