/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data

import net.minecraft.resources.Identifier

public class MinigameDataType<D: MinigameData>(
    public val id: Identifier
) {
    override fun toString(): String {
        return this.id.toString()
    }
}
