package net.casual.arcade.dimensions.level.vanilla

import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

public interface VanillaLikeLevel {
    public val vanillaDimension: VanillaDimension
    public val vanillaDimensionMapper: VanillaDimensionMapper

    public companion object {
        @JvmStatic
        public fun getLikeDimension(level: Level): ResourceKey<Level> {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimension.getDimensionKey()
            }
            return level.dimension()
        }

        @JvmStatic
        public fun getReplacementDimensionFor(level: Level, original: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                val dimension = VanillaDimension.fromDimensionKey(original) ?: return null
                return level.vanillaDimensionMapper.get(dimension)
            }
            return original
        }

        @JvmStatic
        public fun getOverworldDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimensionMapper.get(VanillaDimension.Overworld)
            }
            return fallback
        }

        @JvmStatic
        public fun getNetherDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimensionMapper.get(VanillaDimension.Nether)
            }
            return fallback
        }

        @JvmStatic
        public fun getEndDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.vanillaDimensionMapper.get(VanillaDimension.End)
            }
            return fallback
        }
    }
}

