/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules
import net.casual.arcade.dimensions.level.spawner.CustomSpawnerFactory
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object DimensionRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    @JvmField
    public val CUSTOM_LEVEL_FACTORY: ResourceKey<Registry<MapCodec<out CustomLevelFactory>>> = create("custom_level_factory")
    @JvmField
    public val CUSTOM_SPAWNER_FACTORY: ResourceKey<Registry<MapCodec<out CustomSpawnerFactory>>> = create("custom_spawner_factory")

    @JvmField
    public val CUSTOM_MOB_SPAWNING_RULES: ResourceKey<Registry<CustomMobSpawningRules>> = create("custom_mob_spawning_rules")
}

public object DimensionRegistries: RegistrySupplier() {
    @JvmField
    public val CUSTOM_LEVEL_FACTORY: Registry<MapCodec<out CustomLevelFactory>> = create(DimensionRegistryKeys.CUSTOM_LEVEL_FACTORY, CustomLevelFactory::bootstrap)
    @JvmField
    public val CUSTOM_SPAWNER_FACTORY: Registry<MapCodec<out CustomSpawnerFactory>> = create(DimensionRegistryKeys.CUSTOM_SPAWNER_FACTORY, CustomSpawnerFactory::bootstrap)

    @JvmField
    public val CUSTOM_MOB_SPAWNING_RULES: Registry<CustomMobSpawningRules> = create(DimensionRegistryKeys.CUSTOM_MOB_SPAWNING_RULES, CustomMobSpawningRules::bootstrap)
}