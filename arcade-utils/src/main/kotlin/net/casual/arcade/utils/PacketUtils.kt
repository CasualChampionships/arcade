package net.casual.arcade.utils

import net.casual.arcade.util.mixins.ClientboundPlayerInfoUpdatePacketAccessor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry
import net.minecraft.server.level.ServerPlayer
import java.util.*

public fun ClientboundPlayerInfoUpdatePacket(
    actions: EnumSet<Action>,
    entries: List<Entry>
): ClientboundPlayerInfoUpdatePacket {
    val packet = ClientboundPlayerInfoUpdatePacket(actions, listOf())
    @Suppress("KotlinConstantConditions")
    (packet as ClientboundPlayerInfoUpdatePacketAccessor).setEntries(entries)
    return packet
}

public fun Packet<*>.asClientGamePacket(): Packet<ClientGamePacketListener> {
    @Suppress("UNCHECKED_CAST")
    return this as Packet<ClientGamePacketListener>
}

public inline fun ClientboundBundlePacket.modify(
    player: ServerPlayer,
    modifier: (ServerPlayer, Packet<in ClientGamePacketListener>) -> Packet<in ClientGamePacketListener>
): ClientboundBundlePacket {
    val updated = ArrayList<Packet<in ClientGamePacketListener>>()
    for (sub in this.subPackets()) {
        val new = modifier.invoke(player, sub)
        if (new is ClientboundBundlePacket) {
            updated.addAll(new.subPackets())
        } else {
            updated.add(new)
        }
    }
    return ClientboundBundlePacket(updated)
}