package net.casual.arcade.dimensions.level.vanilla

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

/**
 * Interface for a level which is 'like' a vanilla level.
 *
 * The purpose of this is to allow for custom level
 * creation that behaves like the vanilla overworld,
 * nether, and end dimensions.
 * Portal travel between the dimensions should work
 * correctly, any dimension specific advancements/triggers
 * should also work correctly.
 *
 * @see VanillaLikeLevelsBuilder
 */
public interface VanillaLikeLevel {
    /**
     * The vanilla dimension this level is
     * trying to imitate.
     */
    public val vanillaDimension: VanillaDimension

    /**
     * The vanilla dimension mapper which provides
     * access to all dimensions related to this one.
     */
    public val vanillaDimensionMapper: VanillaDimensionMapper

    public companion object {
        /**
         * Gets the dimension key which [level] is portraying to be.
         *
         * For [VanillaLikeLevel]s this will return the vanilla
         * dimension it's imitating.
         *
         * @param level The level to get the key for.
         * @return The dimension key.
         */
        @JvmStatic
        public fun getLikeDimension(level: Level): ResourceKey<Level> {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimension.getDimensionKey()
            }
            return level.dimension()
        }

        /**
         * Replaces the [original] destination dimension key
         * with one related to [level].
         *
         * For example, if [level] is a nether [VanillaLikeLevel]
         * and [original] is [Level.OVERWORLD] then the dimension
         * key for the overworld for the respective [level]
         * will be returned.
         *
         * @param level The originating level.
         * @param original The original destination dimension key.
         * @return The related dimension key.
         */
        @JvmStatic
        public fun getReplacementDestinationFor(level: Level, original: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                val dimension = VanillaDimension.fromDimensionKey(original) ?: return null
                return level.vanillaDimensionMapper.get(dimension)
            }
            return original
        }

        /**
         * Gets the related overworld dimension key for the
         * specified [level].
         *
         * @param level The originating level.
         * @param fallback The fallback dimension key.
         * @return The related overworld dimension key.
         */
        @JvmStatic
        public fun getOverworldDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimensionMapper.get(VanillaDimension.Overworld)
            }
            return fallback
        }

        /**
         * Gets the related nether dimension key for the
         * specified [level].
         *
         * @param level The originating level.
         * @param fallback The fallback dimension key.
         * @return The related nether dimension key.
         */
        @JvmStatic
        public fun getNetherDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimensionMapper.get(VanillaDimension.Nether)
            }
            return fallback
        }

        /**
         * Gets the related end dimension key for the
         * specified [level].
         *
         * @param level The originating level.
         * @param fallback The fallback dimension key.
         * @return The related end dimension key.
         */
        @JvmStatic
        public fun getEndDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimensionMapper.get(VanillaDimension.End)
            }
            return fallback
        }
    }
}

