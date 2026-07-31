/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals

import net.casual.arcade.virtual.visuals.extensions.*
import net.fabricmc.api.ModInitializer

public object ArcadeVirtualVisuals: ModInitializer {
    override fun onInitialize() {
        PlayerSidebarExtension.registerEvents()
        PlayerTabDisplayExtension.registerEvents()
        PlayerBossbarsExtension.registerEvents()
        PlayerCameraExtension.registerEvents()
        PlayerCameraOverlayExtension.registerEvents()
    }
}