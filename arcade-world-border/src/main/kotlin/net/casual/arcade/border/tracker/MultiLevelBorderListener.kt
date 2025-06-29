/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.tracker

import net.minecraft.server.level.ServerLevel

@Deprecated("This package is deprecated. Use 'net.casual.arcade.boundary' instead.")
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