/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc

import net.casual.arcade.npc.configuration.FakePlayerConfigurationTasks
import net.fabricmc.api.ModInitializer

public object ArcadeNPCs: ModInitializer {
    override fun onInitialize() {
        FakePlayerConfigurationTasks.registerEvents()
    }
}