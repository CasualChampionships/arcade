/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border

import net.casual.arcade.border.extension.LevelBoundaryExtension
import net.casual.arcade.border.utils.BorderRegistries
import net.fabricmc.api.ModInitializer

public object ArcadeLevelBoundaries: ModInitializer {
    override fun onInitialize() {
        BorderRegistries.load()
        LevelBoundaryExtension.registerEvents()
    }
}