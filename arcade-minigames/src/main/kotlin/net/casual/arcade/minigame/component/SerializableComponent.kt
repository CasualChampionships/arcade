/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

public interface SerializableComponent: MinigameComponent {
    public fun save(output: ValueOutput)

    public fun load(input: ValueInput)
}
