/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.debug.network

import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

public interface DebugPacketBroadcaster {
    public fun sendTemporaryTextPacket(
        id: Identifier,
        level: ServerLevel,
        position: Vec3,
        component: Component,
        scale: Float,
        background: Int,
        expiryTicks: Int
    ) { }

    public fun sendTemporaryBoxPacket(
        id: Identifier,
        level: ServerLevel,
        box: AABB,
        faceColor: Int,
        wireframeColor: Int,
        thickness: Float,
        expiryTicks: Int
    ) { }

    public fun sendTemporarySpherePacket(
        id: Identifier,
        level: ServerLevel,
        position: Vec3,
        radius: Double,
        faceColor: Int,
        wireframeColor: Int,
        resolution: Int,
        thickness: Float,
        expiryTicks: Int
    ) { }

    public fun sendTemporaryLinePacket(
        id: Identifier,
        level: ServerLevel,
        head: Vec3,
        tail: Vec3,
        color: Int,
        thickness: Float,
        headArrowSize: Float,
        tailArrowSize: Float,
        expiryTicks: Int
    ) { }

    public companion object {
        private var instance: DebugPacketBroadcaster = object: DebugPacketBroadcaster {}

        @JvmStatic
        public fun getInstance(): DebugPacketBroadcaster {
            return this.instance
        }

        @JvmStatic
        public fun setInstance(broadcaster: DebugPacketBroadcaster) {
            this.instance = broadcaster
        }
    }
}