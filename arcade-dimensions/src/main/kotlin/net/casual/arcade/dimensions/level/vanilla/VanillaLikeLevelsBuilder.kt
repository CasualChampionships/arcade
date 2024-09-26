package net.casual.arcade.dimensions.level.vanilla

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.utils.EnumUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

public class VanillaLikeLevelsBuilder {
    private val builders = EnumUtils.mapOf<VanillaDimension, CustomLevelBuilder>()

    public fun set(dimension: VanillaDimension, builder: CustomLevelBuilder) {
        this.builders[dimension] = builder
    }

    public fun set(dimension: VanillaDimension, block: CustomLevelBuilder.() -> Unit) {
        val builder = CustomLevelBuilder().vanillaDefaults(dimension)
        builder.block()
        this.set(dimension, builder)
    }

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
        @JvmStatic
        public fun build(server: MinecraftServer, block: VanillaLikeLevelsBuilder.() -> Unit): VanillaLikeLevels {
            val builder = VanillaLikeLevelsBuilder()
            builder.block()
            return builder.build(server)
        }
    }
}