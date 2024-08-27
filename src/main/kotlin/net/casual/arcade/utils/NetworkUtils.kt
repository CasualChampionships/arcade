package net.casual.arcade.utils

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import java.util.ArrayList

public object NetworkUtils {
    public fun modifyBundlePacket(
        player: ServerPlayer,
        packet: ClientboundBundlePacket,
        modifier: (ServerPlayer, Packet<in ClientGamePacketListener>) -> Packet<in ClientGamePacketListener>
    ): ClientboundBundlePacket {
        val updated = ArrayList<Packet<in ClientGamePacketListener>>()
        for (sub in packet.subPackets()) {
            val new = modifier.invoke(player, sub)
            if (new is ClientboundBundlePacket) {
                updated.addAll(new.subPackets())
            } else {
                updated.add(new)
            }
        }
        return ClientboundBundlePacket(updated)
    }

    public fun modifySetEntityDataSharedFlagsPacket(
        player: ServerPlayer,
        packet: ClientboundSetEntityDataPacket,
        modifier: (observee: Entity, observer: ServerPlayer, flags: Byte) -> Byte
    ): ClientboundSetEntityDataPacket {
        val observee = player.serverLevel().getEntity(packet.id) ?: return packet

        val items = packet.packedItems
        val data = ArrayList<DataValue<*>>()
        var changed = false
        for (item in items) {
            if (item.id == Entity.DATA_SHARED_FLAGS_ID.id) {
                val flags = item.value as Byte
                val modified = modifier.invoke(observee, player, flags)
                data.add(DataValue.create(Entity.DATA_SHARED_FLAGS_ID, modified))
                changed = true
            } else {
                data.add(item)
            }
        }
        if (!changed) {
            return packet
        }

        val replacement = ClientboundSetEntityDataPacket(packet.id, data)
        // For polymer compatability
        EntityAttachedPacket.set(replacement, observee)
        return replacement
    }
}