/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc

import net.casual.arcade.npc.configuration.FakePlayerConfigurationTasks
import net.casual.arcade.npc.extensions.LevelNavigatingPlayersExtension
import net.casual.arcade.npc.pathfinding.navigation.PathNavigation
import net.fabricmc.api.ModInitializer

public object ArcadeNPCs: ModInitializer {
    public const val MOD_ID: String = "arcade-npcs"

    override fun onInitialize() {
        FakePlayerConfigurationTasks.registerEvents()
        LevelNavigatingPlayersExtension.registerEvents()
        PathNavigation.registerEvents()
    }
}