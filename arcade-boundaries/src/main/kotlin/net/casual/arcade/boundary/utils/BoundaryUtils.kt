/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.utils

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.boundary.extension.LevelBoundaryExtension.Companion.levelBoundaryExtension
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.border.WorldBorder

private val WORLD_BORDER = WorldBorder()

public fun ClientboundSetBorderWarningDistancePacket(blocks: Int): ClientboundSetBorderWarningDistancePacket {
    WORLD_BORDER.warningBlocks = blocks
    return ClientboundSetBorderWarningDistancePacket(WORLD_BORDER)
}

public fun ClientboundSetBorderSizePacket(size: Double): ClientboundSetBorderSizePacket {
    WORLD_BORDER.size = size
    return ClientboundSetBorderSizePacket(WORLD_BORDER)
}

public fun ClientboundSetBorderLerpSizePacket(old: Double, new: Double, ticks: Long): ClientboundSetBorderLerpSizePacket {
    // The game time parameter here is completely useless...
    // They just add the duration to it then subtract the time from it to get back the duration
    WORLD_BORDER.lerpSizeBetween(old, new, ticks, 0)
    return ClientboundSetBorderLerpSizePacket(WORLD_BORDER)
}

public fun ClientboundSetBorderCenterPacket(centerX: Double, centerZ: Double): ClientboundSetBorderCenterPacket {
    WORLD_BORDER.setCenter(centerX, centerZ)
    return ClientboundSetBorderCenterPacket(WORLD_BORDER)
}

/**
 * Gets and sets a [LevelBoundary] for a given [ServerLevel].
 * This may be `null` if no boundary has been set.
 */
public var ServerLevel.levelBoundary: LevelBoundary?
    get() = this.levelBoundaryExtension.getBoundary()
    set(value) {
        val extension = this.levelBoundaryExtension
        if (value == null) extension.removeBoundary() else extension.setBoundary(value)
    }