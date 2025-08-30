/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import io.netty.buffer.ByteBuf
import net.casual.arcade.util.mixins.ClientboundPlayerInfoUpdatePacketAccessor
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.network.protocol.game.*
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.*

public fun ClientboundAddEntityPacket(entity: Entity): ClientboundAddEntityPacket {
    return ClientboundAddEntityPacket(
        entity.id,
        entity.uuid,
        entity.x,
        entity.y,
        entity.z,
        entity.xRot,
        entity.yRot,
        entity.type,
        0,
        entity.deltaMovement,
        entity.yHeadRot.toDouble()
    )
}

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

public fun Packet<*>.getDebugName(): String {
    return if (this is ClientboundCustomPayloadPacket) {
        "CustomPayload(${this.payload.type().id})"
    } else {
        this.type().id.toString()
    }
}

public fun ByteBuf.toByteArray(): ByteArray {
    val bytes = ByteArray(this.readableBytes())
    this.readBytes(bytes)
    return bytes
}