/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.flashback

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.VecDeltaCodec
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public data class ExactEntityPosition(
    val position: Vec3,
    val rotation: Vec2,
    val headRot: Float,
    val onGround: Boolean
) {
    public fun update(packet: ClientboundMoveEntityPacket): ExactEntityPosition {
        var (position, rotation, headRot, _) = this
        if (packet.hasPosition()) {
            val delta = VecDeltaCodec()
            delta.base = this.position
            position = delta.decode(packet.xa.toLong(), packet.ya.toLong(), packet.za.toLong())
        }
        if (packet.hasRotation()) {
            rotation = Vec2(packet.xRot, packet.yRot)
            headRot = packet.yRot
        }
        return ExactEntityPosition(position, rotation, headRot, packet.isOnGround)
    }

    public fun write(buf: FriendlyByteBuf) {
        Vec3.STREAM_CODEC.encode(buf, this.position)
        buf.writeFloat(this.rotation.y)
        buf.writeFloat(this.rotation.x)
        buf.writeFloat(this.headRot)
        buf.writeBoolean(this.onGround)
    }

    public companion object {
        public fun size(): Int {
            // The additional int accounts for the entity id
            return Int.SIZE_BYTES + 3 * Double.SIZE_BYTES + 3 * Float.SIZE_BYTES + 1
        }

        public fun read(buffer: FriendlyByteBuf): ExactEntityPosition {
            val position = Vec3.STREAM_CODEC.decode(buffer)
            val yaw = buffer.readFloat()
            val pitch = buffer.readFloat()
            val headYaw = buffer.readFloat()
            val grounded = buffer.readBoolean()
            return ExactEntityPosition(position, Vec2(pitch, yaw), headYaw, grounded)
        }
    }
}