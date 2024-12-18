/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.vanilla

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.LevelProperties
import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.dimensions.level.factory.CustomLevelFactoryConstructor
import net.casual.arcade.dimensions.level.factory.SimpleCustomLevelFactory
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

/**
 * A [CustomLevelFactory] implementation which supports creating
 * [VanillaLikeCustomLevel] instances.
 *
 * @see CustomLevelFactory
 */
public class VanillaLikeCustomLevelFactory(
    properties: LevelProperties,
    generationOptions: LevelGenerationOptions,
    persistence: LevelPersistence,
    private val vanillaDimension: VanillaDimension,
    private val otherDimensions: VanillaDimensionMapper
): SimpleCustomLevelFactory(properties, generationOptions, persistence) {
    override fun create(server: MinecraftServer, key: ResourceKey<Level>): CustomLevel {
        return VanillaLikeCustomLevel(
            server,
            key,
            this.vanillaDimension,
            this.otherDimensions,
            this.properties,
            this.generationOptions,
            this.persistence,
            this
        )
    }

    override fun codec(): MapCodec<out CustomLevelFactory> {
        return CODEC
    }

    public companion object: CodecProvider<VanillaLikeCustomLevelFactory> {
        override val ID: ResourceLocation = ResourceUtils.arcade("vanilla_like")

        override val CODEC: MapCodec<out VanillaLikeCustomLevelFactory> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                propertiesCodec(), generationOptionsCodec(), persistenceCodec(),
                VanillaDimension.CODEC.fieldOf("vanilla_dimension").forGetter(VanillaLikeCustomLevelFactory::vanillaDimension),
                VanillaDimensionMapper.CODEC.fieldOf("other_dimensions").forGetter(VanillaLikeCustomLevelFactory::otherDimensions)
            ).apply(instance, ::VanillaLikeCustomLevelFactory)
        }

        @JvmStatic
        public fun constructor(
            dimension: VanillaDimension,
            mapper: VanillaDimensionMapper
        ): CustomLevelFactoryConstructor {
            return CustomLevelFactoryConstructor { properties, options, persistence ->
                VanillaLikeCustomLevelFactory(properties, options, persistence, dimension, mapper)
            }
        }
    }
}