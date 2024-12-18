/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.factory

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeCustomLevelFactory
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import java.util.function.Function

/**
 * Factory for creating custom levels.
 *
 * The motivation for this is creating permanent levels
 * which we want to serialize and deserialize.
 * When we deserialize our level, we want to ensure that
 * it creates the same implementation as when it is serialized,
 * thus we need some way of creating these instances.
 *
 * This is not needed if your levels are runtime only,
 * for the majority of cases you will not need to implement
 * this as the default [CustomLevel] will suffice.
 *
 * All [CustomLevelFactory]'s should be registered to
 * [DimensionRegistries.CUSTOM_LEVEL_FACTORY] in your
 * mod initializer.
 *
 * Here's an example:
 * ```
 * class MyCustomLevelFactory: CustomLevelFactory {
 *     override fun create(server: MinecraftServer, key: ResourceKey<Level>): CustomLevel {
 *         // ...
 *     }
 *
 *     override fun codec(): MapCodec<out CustomLevelFactory> {
 *         return CODEC
 *     }
 *
 *     companion object: CodecProvider<CustomLevelFactory> {
 *         override val ID: ResourceLocation = // ...
 *
 *         override val CODEC: MapCodec<out CustomLevelFactory> = // ...
 *     }
 * }
 *
 * class MyMod: ModInitializer {
 *     override fun onInitialize() {
 *         MyCustomLevelFactory.register(DimensionRegistries.CUSTOM_LEVEL_FACTORY)
 *     }
 * }
 * ```
 *
 * @see CustomLevelBuilder.constructor
 * @see CustomLevelFactoryConstructor
 */
public interface CustomLevelFactory {
    public fun create(server: MinecraftServer, key: ResourceKey<Level>): CustomLevel

    public fun codec(): MapCodec<out CustomLevelFactory>

    public companion object {
        public val CODEC: Codec<CustomLevelFactory> = Codec.lazyInitialized {
            DimensionRegistries.CUSTOM_LEVEL_FACTORY.byNameCodec()
                .dispatch(CustomLevelFactory::codec, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out CustomLevelFactory>>) {
            SimpleCustomLevelFactory.register(registry)
            VanillaLikeCustomLevelFactory.register(registry)
        }
    }
}