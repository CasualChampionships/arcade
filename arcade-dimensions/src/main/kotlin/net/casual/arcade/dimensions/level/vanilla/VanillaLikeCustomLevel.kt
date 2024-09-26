package net.casual.arcade.dimensions.level.vanilla

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.LevelProperties
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

public class VanillaLikeCustomLevel internal constructor(
    server: MinecraftServer,
    dimension: ResourceKey<Level>,
    override val vanillaDimension: VanillaLikeLevel.Dimension,
    override val dimensionMapper: VanillaLikeLevel.DimensionMapper,
    properties: LevelProperties,
    options: LevelGenerationOptions,
    persistence: LevelPersistence,
    factory: VanillaLikeCustomLevelFactory,
): CustomLevel(server, dimension, properties, options, persistence, factory), VanillaLikeLevel