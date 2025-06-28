/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.utils

import net.minecraft.network.protocol.game.*
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

public fun ClientboundSetBorderLerpSizePacket(old: Double, new: Double, millis: Long): ClientboundSetBorderLerpSizePacket {
    WORLD_BORDER.lerpSizeBetween(old, new, millis)
    return ClientboundSetBorderLerpSizePacket(WORLD_BORDER)
}

public fun ClientboundSetBorderCenterPacket(centerX: Double, centerZ: Double): ClientboundSetBorderCenterPacket {
    WORLD_BORDER.setCenter(centerX, centerZ)
    return ClientboundSetBorderCenterPacket(WORLD_BORDER)
}