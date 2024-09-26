package net.casual.arcade.dimensions.level.vanilla

import net.minecraft.server.level.ServerLevel

public class VanillaLikeLevels internal constructor(
    private val map: Map<VanillaLikeLevel.Dimension, ServerLevel>
) {
    public fun get(dimension: VanillaLikeLevel.Dimension): ServerLevel? {
        return this.map[dimension]
    }

    public fun getOrThrow(dimension: VanillaLikeLevel.Dimension): ServerLevel {
        return this.get(dimension)
            ?: throw IllegalStateException("Expected dimension $dimension to exist")
    }
}