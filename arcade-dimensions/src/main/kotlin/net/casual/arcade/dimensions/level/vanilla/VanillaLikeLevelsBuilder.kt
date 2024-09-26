package net.casual.arcade.dimensions.level.vanilla

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel.Dimension
import net.casual.arcade.utils.EnumUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

public class VanillaLikeLevelsBuilder {
    private val builders = EnumUtils.mapOf<Dimension, CustomLevelBuilder>()

    public fun set(dimension: Dimension, block: CustomLevelBuilder.() -> Unit) {
        val builder = CustomLevelBuilder()
            .levelStem(dimension.getStemKey())
            .tickTime(dimension.doesTimeTick())
        builder.block()
        this.builders[dimension] = builder
    }

    public fun build(server: MinecraftServer): VanillaLikeLevels {
        val levels = EnumUtils.mapOf<Dimension, CustomLevel>()
        val others = EnumUtils.mapOf<Dimension, ResourceKey<Level>>()
        val mapper = VanillaLikeLevel.DimensionMapper(others)
        for ((dimension, builder) in this.builders) {
            val level = builder.constructor(VanillaLikeCustomLevelFactory.constructor(dimension, mapper))
                .build(server)
            others[dimension] = level.dimension()
            levels[dimension] = level
        }
        return VanillaLikeLevels(levels)
    }
}