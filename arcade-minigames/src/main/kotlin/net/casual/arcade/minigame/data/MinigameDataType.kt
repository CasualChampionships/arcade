/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data

import net.minecraft.resources.Identifier

/**
 * Represents a type for a specific [MinigameData]
 * implementation.
 *
 * @see MinigameData
 */
public class MinigameDataType<D: MinigameData>(
    /**
     * The [id] of the data type.
     */
    public val id: Identifier
) {
    override fun toString(): String {
        return this.id.toString()
    }
}
