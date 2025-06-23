/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.vanilla

import com.mojang.serialization.Codec
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.minecraft.resources.ResourceKey
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level

/**
 * Class which maps vanilla dimensions to a given dimension key.
 *
 * This is used in the [VanillaLikeLevel] to determine
 * the dimension key for a related vanilla dimension.
 *
 * @param map The map of vanilla dimensions to dimension keys.
 */
public class VanillaDimensionMapper(private val map: Map<VanillaDimension, ResourceKey<Level>>) {
    /**
     * Gets the dimension key for the related vanilla dimension.
     *
     * @param dimension The vanilla dimension.
     * @return The dimension key.
     */
    public fun get(dimension: VanillaDimension): ResourceKey<Level>? {
        return this.map[dimension]
    }

    public companion object {
        @JvmField
        public val CODEC: Codec<VanillaDimensionMapper> = OrderedRecordCodecBuilder.create { instance ->
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