package net.casual.arcade.border

import net.minecraft.server.level.ServerLevel

interface MultiLevelBorderListener {
    fun onInitialiseBorder(border: TrackedBorder, level: ServerLevel) { }

    fun onSingleBorderMove(border: TrackedBorder, level: ServerLevel) { }

    fun onSingleBorderComplete(border: TrackedBorder, level: ServerLevel) { }

    fun onAllBordersComplete(border: TrackedBorder, level: ServerLevel) { }

    fun onAllBordersComplete(borders: Map<TrackedBorder, ServerLevel>) {
        for ((border, level) in borders) {
            this.onAllBordersComplete(border, level)
        }
    }

    fun onInitialiseBorder(borders: Map<TrackedBorder, ServerLevel>) {
        for ((border, level) in borders) {
            this.onInitialiseBorder(border, level)
        }
    }
}