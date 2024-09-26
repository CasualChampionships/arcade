package net.casual.arcade.dimensions.level.factory

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.LevelProperties
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

public open class SimpleCustomLevelFactory(
    public val properties: LevelProperties,
    public val generationOptions: LevelGenerationOptions,
    public val persistence: LevelPersistence
): CustomLevelFactory {
    public override fun create(server: MinecraftServer, dimension: ResourceKey<Level>): CustomLevel {
        return CustomLevel(server, dimension, this.properties, this.generationOptions, this.persistence, this)
    }

    override fun codec(): MapCodec<out CustomLevelFactory> {
        return CODEC
    }

    public companion object: CodecProvider<SimpleCustomLevelFactory> {
        override val ID: ResourceLocation = ResourceUtils.arcade("simple")

        public override val CODEC: MapCodec<SimpleCustomLevelFactory> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                propertiesCodec(), generationOptionsCodec(), persistenceCodec()
            ).apply(instance, ::SimpleCustomLevelFactory)
        }

        public fun <T: SimpleCustomLevelFactory> propertiesCodec(): RecordCodecBuilder<T, LevelProperties> {
            return LevelProperties.CODEC.fieldOf("properties").forGetter(SimpleCustomLevelFactory::properties)
        }

        public fun <T: SimpleCustomLevelFactory> generationOptionsCodec(): RecordCodecBuilder<T, LevelGenerationOptions> {
            return LevelGenerationOptions.CODEC.fieldOf("options").forGetter(SimpleCustomLevelFactory::generationOptions)
        }

        public fun <T: SimpleCustomLevelFactory> persistenceCodec(): RecordCodecBuilder<T, LevelPersistence> {
            return LevelPersistence.CODEC.fieldOf("persistence").forGetter(SimpleCustomLevelFactory::persistence)
        }
    }
}