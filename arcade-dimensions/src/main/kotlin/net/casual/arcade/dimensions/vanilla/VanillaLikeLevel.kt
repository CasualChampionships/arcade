package net.casual.arcade.dimensions.vanilla

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

public interface VanillaLikeLevel {
    public val vanilla: VanillaDimension
    public val others: VanillaLikeDimensions

    public companion object {
        @JvmStatic
        public fun getLikeDimension(level: Level): ResourceKey<Level> {
            if (level is VanillaLikeLevel) {
                return level.vanilla.key
            }
            return level.dimension()
        }

        @JvmStatic
        public fun getReplacementDimensionFor(level: Level, original: ResourceKey<Level>): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return when (original) {
                    Level.OVERWORLD -> level.others.overworld
                    Level.NETHER -> level.others.nether
                    Level.END -> level.others.end
                    else -> null
                }
            }
            return original
        }

        @JvmStatic
        public fun getOverworldDimensionFor(level: Level, fallback: ResourceKey<Level>): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.others.overworld
            }
            return fallback
        }

        @JvmStatic
        public fun getNetherDimensionFor(level: Level, fallback: ResourceKey<Level>): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.others.nether
            }
            return fallback
        }

        @JvmStatic
        public fun getEndDimensionFor(level: Level, fallback: ResourceKey<Level>): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.others.end
            }
            return fallback
        }
    }
}