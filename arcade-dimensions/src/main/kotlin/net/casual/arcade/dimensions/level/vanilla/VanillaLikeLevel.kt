package net.casual.arcade.dimensions.level.vanilla

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.resources.ResourceKey
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level
import net.minecraft.world.level.dimension.LevelStem

public interface VanillaLikeLevel {
    public val vanillaDimension: Dimension
    public val dimensionMapper: DimensionMapper

    public enum class Dimension: StringRepresentable {
        Overworld,
        Nether,
        End;

        public fun getDimensionKey(): ResourceKey<Level> {
            return when(this) {
                Overworld -> Level.OVERWORLD
                Nether -> Level.NETHER
                End -> Level.END
            }
        }

        public fun getStemKey(): ResourceKey<LevelStem> {
            return when(this) {
                Overworld -> LevelStem.OVERWORLD
                Nether -> LevelStem.NETHER
                End -> LevelStem.END
            }
        }

        public fun doesTimeTick(): Boolean {
            return this == Overworld
        }

        override fun getSerializedName(): String {
            return this.getDimensionKey().location().toString()
        }

        public companion object {
            public val CODEC: Codec<Dimension> = StringRepresentable.fromEnum(Dimension::values)

            public fun fromDimensionKey(key: ResourceKey<Level>?): Dimension? {
                return when (key) {
                    Level.OVERWORLD -> Overworld
                    Level.NETHER -> Nether
                    Level.END -> End
                    else -> null
                }
            }
        }
    }

    public class DimensionMapper(private val map: Map<Dimension, ResourceKey<Level>>) {
        public fun get(dimension: Dimension): ResourceKey<Level>? {
            return this.map[dimension]
        }

        public companion object {
            public val CODEC: Codec<DimensionMapper> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.simpleMap(
                        Dimension.CODEC,
                        ArcadeExtraCodecs.DIMENSION,
                        StringRepresentable.keys(Dimension.entries.toTypedArray())
                    ).forGetter(DimensionMapper::map)
                ).apply(instance, ::DimensionMapper)
            }
        }
    }

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
                val dimension = Dimension.fromDimensionKey(original) ?: return null
                return level.dimensionMapper.get(dimension)
            }
            return original
        }

        @JvmStatic
        public fun getOverworldDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.dimensionMapper.get(Dimension.Overworld)
            }
            return fallback
        }

        @JvmStatic
        public fun getNetherDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.dimensionMapper.get(Dimension.Nether)
            }
            return fallback
        }

        @JvmStatic
        public fun getEndDimensionFor(level: Level, fallback: ResourceKey<Level>?): ResourceKey<Level>? {
            if (level is VanillaLikeLevel) {
                return level.dimensionMapper.get(Dimension.End)
            }
            return fallback
        }
    }
}

