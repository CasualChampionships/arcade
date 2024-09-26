package net.casual.arcade.dimensions.level.factory

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeCustomLevelFactory
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import java.util.function.Function

public interface CustomLevelFactory {
    public fun create(server: MinecraftServer, dimension: ResourceKey<Level>): CustomLevel

    public fun codec(): MapCodec<out CustomLevelFactory>

    public companion object {
        public val CODEC: Codec<CustomLevelFactory> by lazy {
            DimensionRegistries.CUSTOM_LEVEL_FACTORY.byNameCodec()
                .dispatch(CustomLevelFactory::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out CustomLevelFactory>>) {
            SimpleCustomLevelFactory.register(registry)
            VanillaLikeCustomLevelFactory.register(registry)
        }
    }
}