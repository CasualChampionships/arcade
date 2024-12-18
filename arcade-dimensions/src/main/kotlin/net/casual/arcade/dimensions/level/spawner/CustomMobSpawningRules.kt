/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.spawner

import com.mojang.serialization.Codec
import net.casual.arcade.dimensions.ducks.CustomMobSpawningPredicate
import net.casual.arcade.dimensions.mixins.level.spawning.NaturalSpawnerAccessor
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.core.Registry
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.LocalMobCapCalculator

public interface CustomMobSpawningRules {
    public fun canCategorySpawn(
        category: MobCategory,
        pos: ChunkPos,
        currentCategoryCount: Int,
        spawnableChunks: Int,
        calculator: LocalMobCapCalculator
    ): Boolean {
        val count = this.getChunkMobCapFor(category) * spawnableChunks / MAGIC_NUMBER
        if (currentCategoryCount >= count) {
            return false
        }
        return calculator.canSpawn(category, pos, this)
    }

    public fun getChunkMobCapFor(category: MobCategory): Int {
        return category.maxInstancesPerChunk
    }

    public companion object {
        public val MAGIC_NUMBER: Int = NaturalSpawnerAccessor.getMagicNumber()

        public val CODEC: Codec<CustomMobSpawningRules> = Codec.lazyInitialized {
            DimensionRegistries.CUSTOM_MOB_SPAWNING_RULES.byNameCodec()
        }

        public val DEFAULT: CustomMobSpawningRules = object: CustomMobSpawningRules {}

        @JvmStatic
        public fun LocalMobCapCalculator.canSpawn(
            category: MobCategory,
            pos: ChunkPos,
            rules: CustomMobSpawningRules
        ): Boolean {
            return (this as CustomMobSpawningPredicate).`arcade$canSpawn`(category, pos, rules)
        }

        internal fun bootstrap(registry: Registry<CustomMobSpawningRules>) {
            Registry.register(registry, ResourceUtils.arcade("vanilla"), DEFAULT)
        }
    }
}