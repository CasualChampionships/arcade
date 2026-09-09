/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.utils.ClientboundRotateHeadPacket
import net.casual.arcade.virtual.entity.mixins.ServerEntityAccessor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket
import net.minecraft.network.protocol.game.VecDelta
import net.minecraft.network.protocol.game.VecDeltaCodec
import net.minecraft.util.Mth
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.entity.PositionPath
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

public object VirtualEntityPacketUtils {
    private val POS_TOLERANCE = ServerEntityAccessor.accessToleranceLevelPosition()
    private val ROT_TOLERANCE = ServerEntityAccessor.accessToleranceLevelRotation()

    // TODO: This doesn't match up with ServerEntity's logic anymore.
    //   They have additional logic for ItemEntity's as well as for
    //   position stepping which we don't yet support.
    public fun createMovePacket(id: Int, oldPos: Vec3, newPos: Vec3, oldRot: Vec2, newRot: Vec2): Packet<*>? {
        val codec = VecDeltaCodec()
        codec.base = oldPos

        val delta = codec.tryEncode(newPos)
            ?: return ClientboundEntityPositionSyncPacket(id, PositionPath.of(newPos), newRot.y, newRot.x, false)

        val oldXRot = Mth.packDegrees(oldRot.x)
        val oldYRot = Mth.packDegrees(oldRot.y)
        val newXRot = Mth.packDegrees(newRot.x)
        val newYRot = Mth.packDegrees(newRot.y)

        val shouldSendPosition = codec.delta(newPos).lengthSqr() >= POS_TOLERANCE
        val shouldSendRotation = abs(newYRot - oldYRot) >= ROT_TOLERANCE || abs(newXRot - oldXRot) >= ROT_TOLERANCE

        if (shouldSendPosition && shouldSendRotation) {
            return ClientboundMoveEntityPacket.PosRot(id, delta, newYRot, newXRot, false)
        }
        if (shouldSendPosition) {
            return ClientboundMoveEntityPacket.Pos(id, delta, false)
        }
        if (shouldSendRotation) {
            return ClientboundMoveEntityPacket.Pos(id, delta, false)
        }
        return null
    }

    public fun createRotationPacket(id: Int, oldRot: Vec2, newRot: Vec2): ClientboundMoveEntityPacket.Rot? {
        val oldXRot = Mth.packDegrees(oldRot.x)
        val oldYRot = Mth.packDegrees(oldRot.y)
        val newXRot = Mth.packDegrees(newRot.x)
        val newYRot = Mth.packDegrees(newRot.y)
        if (abs(newYRot - oldYRot) >= 1 || abs(newXRot - oldXRot) >= 1) {
            return ClientboundMoveEntityPacket.Rot(id, newYRot, newXRot, false)
        }
        return null
    }

    public fun createHeadRotationPacket(id: Int, oldRot: Float, newRot: Float): ClientboundRotateHeadPacket? {
        val oldHeadRot = Mth.packDegrees(oldRot)
        val newHeadRot = Mth.packDegrees(newRot)
        if (abs(oldHeadRot - newHeadRot) >= 1) {
            return ClientboundRotateHeadPacket(id, newHeadRot)
        }
        return null
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
}