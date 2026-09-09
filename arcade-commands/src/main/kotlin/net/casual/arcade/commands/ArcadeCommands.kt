/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.commands

import net.casual.arcade.commands.manager.GlobalCommandManager
import net.fabricmc.api.ModInitializer

public object ArcadeCommands: ModInitializer {
    override fun onInitialize() {
        GlobalCommandManager.registerEvents()
    }
}