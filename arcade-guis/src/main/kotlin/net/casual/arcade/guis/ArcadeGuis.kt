/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis

import net.casual.arcade.guis.hotbar.PlayerHotbarGuiExtension
import net.fabricmc.api.ModInitializer

public object ArcadeGuis: ModInitializer {
    override fun onInitialize() {
        PlayerHotbarGuiExtension.registerEvents()
    }
}