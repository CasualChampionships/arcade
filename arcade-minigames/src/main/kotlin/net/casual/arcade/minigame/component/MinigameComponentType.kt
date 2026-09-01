/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.minecraft.resources.Identifier

public class MinigameComponentType<C: MinigameComponent>(
    public val id: Identifier
) {
    override fun toString(): String {
        return this.id.toString()
    }
}
