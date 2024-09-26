package net.casual.arcade.dimensions.level.vanilla

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.resources.ResourceKey
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level

public class VanillaDimensionMapper(private val map: Map<VanillaDimension, ResourceKey<Level>>) {
    public fun get(dimension: VanillaDimension): ResourceKey<Level>? {
        return this.map[dimension]
    }

    public companion object {
        @JvmField
        public val CODEC: Codec<VanillaDimensionMapper> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.simpleMap(
                    VanillaDimension.CODEC,
                    ArcadeExtraCodecs.DIMENSION,
                    StringRepresentable.keys(VanillaDimension.entries.toTypedArray())
                ).forGetter(VanillaDimensionMapper::map)
            ).apply(instance, ::VanillaDimensionMapper)
        }
    }
}