/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals

import net.casual.arcade.visuals.extensions.PlayerBossbarsExtension
import net.casual.arcade.visuals.extensions.PlayerCameraExtension
import net.casual.arcade.visuals.extensions.PlayerCameraOverlayExtension
import net.casual.arcade.visuals.extensions.PlayerSidebarExtension
import net.casual.arcade.visuals.extensions.PlayerTabDisplayExtension
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PlayerLookup

public object ArcadeVisuals: ModInitializer {
    override fun onInitialize() {
        PlayerSidebarExtension.registerEvents()
        PlayerTabDisplayExtension.registerEvents()
        PlayerBossbarsExtension.registerEvents()
        PlayerCameraExtension.registerEvents()
        PlayerCameraOverlayExtension.registerEvents()
    }
}