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

public class VanillaLikeCustomLevelFactory(
    properties: LevelProperties,
    generationOptions: LevelGenerationOptions,
    persistence: LevelPersistence,
    private val vanillaDimension: VanillaLikeLevel.Dimension,
    private val otherDimensions: VanillaLikeLevel.DimensionMapper
): SimpleCustomLevelFactory(properties, generationOptions, persistence) {
    override fun create(server: MinecraftServer, dimension: ResourceKey<Level>): CustomLevel {
        return VanillaLikeCustomLevel(
            server,
            dimension,
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
                VanillaLikeLevel.Dimension.CODEC.fieldOf("vanilla_dimension").forGetter(VanillaLikeCustomLevelFactory::vanillaDimension),
                VanillaLikeLevel.DimensionMapper.CODEC.fieldOf("other_dimensions").forGetter(VanillaLikeCustomLevelFactory::otherDimensions)
            ).apply(instance, ::VanillaLikeCustomLevelFactory)
        }

        public fun constructor(
            dimension: VanillaLikeLevel.Dimension,
            mapper: VanillaLikeLevel.DimensionMapper
        ): CustomLevelFactoryConstructor {
            return CustomLevelFactoryConstructor { properties, options, persistence ->
                VanillaLikeCustomLevelFactory(properties, options, persistence, dimension, mapper)
            }
        }
    }
}