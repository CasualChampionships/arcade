package net.casual.arcade.dimensions.level.vanilla

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.LevelProperties
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level

/**
 * A [CustomLevel] implementation which supports [VanillaLikeLevel].
 *
 * @see VanillaLikeLevel
 */
public class VanillaLikeCustomLevel internal constructor(
    server: MinecraftServer,
    key: ResourceKey<Level>,
    override val vanillaDimension: VanillaDimension,
    override val vanillaDimensionMapper: VanillaDimensionMapper,
    properties: LevelProperties,
    options: LevelGenerationOptions,
    persistence: LevelPersistence,
    factory: VanillaLikeCustomLevelFactory,
): CustomLevel(server, key, properties, options, persistence, factory), VanillaLikeLevel