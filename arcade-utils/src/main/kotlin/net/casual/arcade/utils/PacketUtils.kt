/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import net.casual.arcade.util.mixins.ClientboundPlayerInfoUpdatePacketAccessor
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
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

public fun ClientboundLevelParticlesPacket(
    options: ParticleOptions,
    position: Vec3,
    xDist: Float = 0.0F,
    yDist: Float = 0.0F,
    zDist: Float = 0.0F,
    speed: Float = 0.0F,
    count: Int = 0,
    alwaysRender: Boolean = false,
    overrideLimiter: Boolean = false
): ClientboundLevelParticlesPacket {
    return ClientboundLevelParticlesPacket(
        options, overrideLimiter, alwaysRender, position.x, position.y, position.z, xDist, yDist, zDist, speed, count
    )
}

public fun Packet<*>.asClientGamePacket(): Packet<ClientGamePacketListener> {
    @Suppress("UNCHECKED_CAST")
    return this as Packet<ClientGamePacketListener>
}

public inline fun ClientboundBundlePacket.modify(
    player: ServerPlayer,
    modifier: (ServerPlayer, Packet<in ClientGamePacketListener>) -> Packet<in ClientGamePacketListener>?
): ClientboundBundlePacket {
    val updated = ArrayList<Packet<in ClientGamePacketListener>>()
    for (sub in this.subPackets()) {
        val new = modifier.invoke(player, sub) ?: continue
        if (new is ClientboundBundlePacket) {
            updated.addAll(new.subPackets())
        } else {
            updated.add(new)
        }
    }
    return ClientboundBundlePacket(updated)
}