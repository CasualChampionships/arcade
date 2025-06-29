/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary

import net.casual.arcade.boundary.extension.LevelBoundaryExtension
import net.casual.arcade.boundary.utils.BoundaryRegistries
import net.fabricmc.api.ModInitializer

public object ArcadeLevelBoundaries: ModInitializer {
    override fun onInitialize() {
        BoundaryRegistries.load()
        LevelBoundaryExtension.registerEvents()
    }
}