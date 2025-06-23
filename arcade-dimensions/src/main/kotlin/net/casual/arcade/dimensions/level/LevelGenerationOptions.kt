/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.spawner.CustomSpawnerFactory
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryFileCodec
import net.minecraft.world.level.dimension.LevelStem

/**
 * This class contains all the options for world generation.
 *
 * @param stem The stem of the level.
 * @param seed The seed of the level.
 * @param flat Whether the level is flat.
 * @param tickTime Whether the time should tick.
 * @param generateStructures Whether structures should be generated.
 * @param debug Whether the world is the [debug world](https://minecraft.wiki/w/Debug_mode).
 * @param customSpawners The custom spawners.
 *
 * @see CustomLevelBuilder
 */
public class LevelGenerationOptions(
    public val stem: Holder<LevelStem>,
    public val seed: Long,
    public val flat: Boolean,
    public val tickTime: Boolean,
    public val generateStructures: Boolean,
    public val debug: Boolean,
    public val customSpawners: List<CustomSpawnerFactory>
) {
    public companion object {
        @JvmField
        public val CODEC: Codec<LevelGenerationOptions> = OrderedRecordCodecBuilder.create { instance ->
            instance.group(
                RegistryFileCodec.create(Registries.LEVEL_STEM, LevelStem.CODEC).fieldOf("stem").forGetter(LevelGenerationOptions::stem),
                Codec.LONG.fieldOf("seed").forGetter(LevelGenerationOptions::seed),
                Codec.BOOL.fieldOf("flat").forGetter(LevelGenerationOptions::flat),
                Codec.BOOL.fieldOf("tick_time").forGetter(LevelGenerationOptions::tickTime),
                Codec.BOOL.fieldOf("generate_structures").forGetter(LevelGenerationOptions::generateStructures),
                Codec.BOOL.fieldOf("debug").forGetter(LevelGenerationOptions::debug),
                CustomSpawnerFactory.CODEC.listOf().fieldOf("custom_spawners").forGetter(LevelGenerationOptions::customSpawners)
            ).apply(instance, ::LevelGenerationOptions)
        }
    }
}