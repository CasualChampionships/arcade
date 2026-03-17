/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.utils.impl

import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelProperties
import net.minecraft.world.Difficulty
import net.minecraft.world.level.storage.DerivedLevelData
import net.minecraft.world.level.storage.ServerLevelData
import net.minecraft.world.level.storage.WorldData
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
internal class DerivedLevelData(
    val properties: LevelProperties,
    val options: LevelGenerationOptions,
    worldData: WorldData,
    levelData: ServerLevelData
): DerivedLevelData(worldData, levelData) {
    override fun getDifficulty(): Difficulty {
        return this.properties.difficulty.map { it.value }
            .orElseGet { super.getDifficulty() }
    }

    override fun isDifficultyLocked(): Boolean {
        return this.properties.difficulty.map { it.locked }
            .orElseGet { super.isDifficultyLocked() }
    }
}