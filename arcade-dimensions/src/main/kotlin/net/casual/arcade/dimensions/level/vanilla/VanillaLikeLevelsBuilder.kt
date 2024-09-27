package net.casual.arcade.dimensions.level.vanilla

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.utils.EnumUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

/**
 * Builder class for creating [VanillaLikeLevels].
 *
 * This allows you to set [CustomLevelBuilder]s for each
 * [VanillaDimension] and then have constructed into
 * [VanillaLikeCustomLevel]s.
 *
 * Not all [VanillaDimension]s need to be specified, any
 * unspecified dimensions will be absent from [VanillaLikeLevels].
 *
 * @see VanillaLikeLevel
 */
public class VanillaLikeLevelsBuilder {
    private val builders = EnumUtils.mapOf<VanillaDimension, CustomLevelBuilder>()

    /**
     * Sets the [CustomLevelBuilder] for the given [dimension].
     *
     * @param dimension The dimension to build.
     * @param builder The level builder.
     * @return This builder.
     */
    public fun set(dimension: VanillaDimension, builder: CustomLevelBuilder): VanillaLikeLevelsBuilder {
        this.builders[dimension] = builder
        return this
    }

    /**
     * Sets the [CustomLevelBuilder] for the given [dimension].
     *
     * @param dimension The dimension to build.
     * @param block The method to configure the builder.
     * @return This builder.
     */
    public fun set(dimension: VanillaDimension, block: CustomLevelBuilder.() -> Unit): VanillaLikeLevelsBuilder {
        val builder = CustomLevelBuilder().vanillaDefaults(dimension)
        builder.block()
        return this.set(dimension, builder)
    }

    /**
     * Builds the [VanillaLikeLevels] from the set builders.
     *
     * This **does not** add the levels to the server, you must
     * do this yourself.
     *
     * ```
     * val levels = VanillaLikeLevelsBuilder.build(server) {
     *     // ...
     * }
     * for (level in levels.all()) {
     *     server.addCustomLevel(level)
     * }
     * ```
     *
     * @param server The server to build the levels for.
     * @return The constructed levels.
     */
    public fun build(server: MinecraftServer): VanillaLikeLevels {
        val levels = EnumUtils.mapOf<VanillaDimension, CustomLevel>()
        val others = EnumUtils.mapOf<VanillaDimension, ResourceKey<Level>>()
        val mapper = VanillaDimensionMapper(others)
        for ((dimension, builder) in this.builders) {
            val level = builder.constructor(VanillaLikeCustomLevelFactory.constructor(dimension, mapper))
                .build(server)
            others[dimension] = level.dimension()
            levels[dimension] = level
        }
        return VanillaLikeLevels(levels)
    }

    public companion object {
        /**
         * Builds a [VanillaLikeLevels] from the given [block].
         *
         * This **does not** add the levels to the server, you must
         * do this yourself.
         *
         * ```
         * val levels = VanillaLikeLevelsBuilder.build(server) {
         *     // ...
         * }
         * for (level in levels.all()) {
         *     server.addCustomLevel(level)
         * }
         * ```
         *
         * @param server The server to build the levels for.
         * @param block The method to configure the builder.
         * @return The constructed levels.
         */
        @JvmStatic
        public fun build(server: MinecraftServer, block: VanillaLikeLevelsBuilder.() -> Unit): VanillaLikeLevels {
            val builder = VanillaLikeLevelsBuilder()
            builder.block()
            return builder.build(server)
        }
    }
}