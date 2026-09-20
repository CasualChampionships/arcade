/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.flashback

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public data class ExactEntityPosition(
    val position: Vec3,
    val rotation: Vec2,
    val headRot: Float,
    val onGround: Boolean
) {
    public fun lerp(other: ExactEntityPosition, delta: Float): ExactEntityPosition {
        return ExactEntityPosition(
            this.position.lerp(other.position, delta.toDouble()),
            Vec2(
                Mth.lerp(delta, this.rotation.x, other.rotation.x),
                Mth.rotLerp(delta, this.rotation.y, other.rotation.y)
            ),
            Mth.rotLerp(delta, this.headRot, other.headRot),
            other.onGround
        )
    }

    public fun isSamePositionAndRotation(other: ExactEntityPosition): Boolean {
        return this.position == other.position && this.rotation == other.rotation
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