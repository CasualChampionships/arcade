/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.minecraft.resources.Identifier

/**
 * Represents a type for a specific [MinigameComponent]
 * implementation.
 *
 * @see MinigameComponent
 */
public class MinigameComponentType<C: MinigameComponent>(
    /**
     * The [id] of the component type.
     */
    public val id: Identifier
) {
    override fun toString(): String {
        return this.id.toString()
    }
}
