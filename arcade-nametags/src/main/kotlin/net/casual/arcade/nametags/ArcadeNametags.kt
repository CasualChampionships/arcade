/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags

import net.casual.arcade.nametags.extensions.EntityNametagExtension
import net.fabricmc.api.ModInitializer

public object ArcadeNametags: ModInitializer {
    override fun onInitialize() {
        EntityNametagExtension.registerEvents()
    }
}