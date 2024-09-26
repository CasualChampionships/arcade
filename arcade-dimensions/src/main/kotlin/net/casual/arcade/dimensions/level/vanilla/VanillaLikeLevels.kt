package net.casual.arcade.dimensions.level.vanilla

import net.casual.arcade.dimensions.level.CustomLevel
import net.minecraft.server.MinecraftServer

public class VanillaLikeLevels internal constructor(
    private val map: Map<VanillaDimension, CustomLevel>
) {
    public fun get(dimension: VanillaDimension): CustomLevel? {
        return this.map[dimension]
    }

    public fun getOrThrow(dimension: VanillaDimension): CustomLevel {
        return this.get(dimension)
            ?: throw IllegalStateException("Expected dimension $dimension to exist")
    }

    public fun all(): Collection<CustomLevel> {
        return this.map.values
    }

    public companion object {
        @JvmStatic
        public fun create(server: MinecraftServer, block: VanillaLikeLevelsBuilder.() -> Unit): VanillaLikeLevels {
            val builder = VanillaLikeLevelsBuilder()
            builder.block()
            return builder.build(server)
        }
    }
}