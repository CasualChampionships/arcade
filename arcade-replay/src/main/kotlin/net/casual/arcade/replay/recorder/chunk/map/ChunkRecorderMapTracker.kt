/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.chunk.map

import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket
import net.minecraft.world.level.saveddata.maps.MapDecoration
import net.minecraft.world.level.saveddata.maps.MapId
import net.minecraft.world.level.saveddata.maps.MapItemSavedData
import kotlin.math.max
import kotlin.math.min

// Based on MapItemSavedData#HoldingPlayer
public class ChunkRecorderMapTracker(
    private val data: MapItemSavedData
) {
    private var colorsDirty = true

    private var minDirtyX = 0
    private var minDirtyY = 0
    private var maxDirtyX = 127
    private var maxDirtyY = 127

    private var decorationsDirty = true

    public fun createNextUpdatePacket(id: MapId): ClientboundMapItemDataPacket? {
        var patch: MapItemSavedData.MapPatch? = null
        if (this.colorsDirty) {
            this.colorsDirty = false
            patch = this.createPatch()
        }
        var decorations: List<MapDecoration>? = null
        if (this.decorationsDirty) {
            this.decorationsDirty = false
            decorations = this.data.decorations.toList()
        }
        if (decorations != null || patch != null) {
            return ClientboundMapItemDataPacket(id, this.data.scale, this.data.locked, decorations, patch)
        }
        return null
    }

    public fun markColorsDirty(x: Int, y: Int) {
        if (this.colorsDirty) {
            this.minDirtyX = min(this.minDirtyX, x)
            this.minDirtyY = min(this.minDirtyY, y)
            this.maxDirtyX = max(this.maxDirtyX, x)
            this.maxDirtyY = max(this.maxDirtyY, y)
        } else {
            this.colorsDirty = true
            this.minDirtyX = x
            this.minDirtyY = y
            this.maxDirtyX = x
            this.maxDirtyY = y
        }
    }

    public fun markDecorationsDirty() {
        this.decorationsDirty = true
    }

    private fun createPatch(): MapItemSavedData.MapPatch {
        val startX = this.minDirtyX
        val startY = this.minDirtyY
        val width = this.maxDirtyX + 1 - this.minDirtyX
        val height = this.maxDirtyY + 1 - this.minDirtyY
        val patch = ByteArray(width * height)

        for (m in 0..<width) {
            for (n in 0..<height) {
                patch[m + n * width] = this.data.colors[startX + m + (startY + n) * 128]
            }
        }

        return MapItemSavedData.MapPatch(startX, startY, width, height, patch)
    }
}