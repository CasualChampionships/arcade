/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals

import net.casual.arcade.visuals.extensions.PlayerBossbarsExtension
import net.casual.arcade.visuals.extensions.PlayerSidebarExtension
import net.casual.arcade.visuals.extensions.PlayerTabDisplayExtension
import net.fabricmc.api.ModInitializer

public object ArcadeVisuals: ModInitializer {
    override fun onInitialize() {
        PlayerSidebarExtension.registerEvents()
        PlayerTabDisplayExtension.registerEvents()
        PlayerBossbarsExtension.registerEvents()
    }
}