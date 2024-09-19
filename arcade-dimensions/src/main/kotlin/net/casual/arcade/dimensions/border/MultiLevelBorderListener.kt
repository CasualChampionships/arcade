package net.casual.arcade.dimensions.border

import net.minecraft.server.level.ServerLevel

public interface MultiLevelBorderListener {
    public fun onInitialiseBorder(border: TrackedBorder, level: ServerLevel) { }

    public fun onSingleBorderActive(border: TrackedBorder, level: ServerLevel) { }

    public fun onSingleBorderComplete(border: TrackedBorder, level: ServerLevel) { }

    public fun onAllBordersComplete(border: TrackedBorder, level: ServerLevel) { }

    public fun onAllBordersComplete(borders: Map<TrackedBorder, ServerLevel>) {
        for ((border, level) in borders) {
            this.onAllBordersComplete(border, level)
        }
    }

    public fun onInitialiseBorder(borders: Map<TrackedBorder, ServerLevel>) {
        for ((border, level) in borders) {
            this.onInitialiseBorder(border, level)
        }
    }
}