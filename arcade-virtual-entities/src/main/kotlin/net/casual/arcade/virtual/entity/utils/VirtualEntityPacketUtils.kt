package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.virtual.entity.mixins.ServerEntityAccessor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.VecDeltaCodec
import net.minecraft.util.Mth
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

public object VirtualEntityPacketUtils {
    public fun createMovePacket(id: Int, oldPos: Vec3, newPos: Vec3, oldRot: Vec2, newRot: Vec2): Packet<*>? {
        val codec = VecDeltaCodec()
        codec.base = oldPos

        val xa = codec.encodeX(newPos)
        val ya = codec.encodeY(newPos)
        val za = codec.encodeZ(newPos)
        if (this.isPosDeltaTooBig(xa) || this.isPosDeltaTooBig(ya) || this.isPosDeltaTooBig(za)) {
            return ClientboundEntityPositionSyncPacket(id, this.createPositionMoveRotation(newPos, newRot), false)
        }

        val oldXRot = Mth.packDegrees(oldRot.x)
        val oldYRot = Mth.packDegrees(oldRot.y)
        val newXRot = Mth.packDegrees(newRot.x)
        val newYRot = Mth.packDegrees(newRot.y)
        val isRotDeltaTooSmall = abs(newYRot - oldYRot) < 1 && abs(newXRot - oldXRot) < 1

        val isPosDeltaTooSmall = codec.delta(newPos).lengthSqr() < ServerEntityAccessor.accessToleranceLevelPosition()

        if (isPosDeltaTooSmall && isRotDeltaTooSmall) {
            return null
        }
        if (isRotDeltaTooSmall) {
            return ClientboundMoveEntityPacket.Pos(id, xa.toShort(), ya.toShort(), za.toShort(), false)
        }
        if (isPosDeltaTooSmall) {
            return ClientboundMoveEntityPacket.Rot(id, newYRot, newXRot, false)
        }
        return ClientboundMoveEntityPacket.PosRot(id, xa.toShort(), ya.toShort(), za.toShort(), newYRot, newXRot, false)
    }

    public fun createRotationPacket(id: Int, rotation: Vec2): ClientboundMoveEntityPacket.Rot {
        return ClientboundMoveEntityPacket.Rot(
            id, Mth.packDegrees(rotation.y), Mth.packDegrees(rotation.x), false
        )
    }

    public fun createPositionMoveRotation(pos: Vec3, rot: Vec2): PositionMoveRotation {
        return PositionMoveRotation(pos, Vec3.ZERO, rot.y, rot.x)
    }

    public fun isEntityPositionPacket(packet: Packet<*>): Boolean {
        return packet is ClientboundMoveEntityPacket.Pos
            || packet is ClientboundMoveEntityPacket.PosRot
            || packet is ClientboundEntityPositionSyncPacket
    }

    public fun isEntityRotationPacket(packet: Packet<*>): Boolean {
        return packet is ClientboundMoveEntityPacket.Rot
            || packet is ClientboundMoveEntityPacket.PosRot
            || packet is ClientboundEntityPositionSyncPacket
    }

    private fun isPosDeltaTooBig(delta: Long): Boolean {
        return delta < Short.MIN_VALUE || delta > Short.MAX_VALUE
    }
}