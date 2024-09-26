package net.casual.arcade.dimensions.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object DimensionRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    @JvmField
    public val CUSTOM_LEVEL_FACTORY: ResourceKey<Registry<MapCodec<out CustomLevelFactory>>> = create("custom_level_factory")
}

public object DimensionRegistries: RegistrySupplier() {
    @JvmField
    public val CUSTOM_LEVEL_FACTORY: Registry<MapCodec<out CustomLevelFactory>> = create(DimensionRegistryKeys.CUSTOM_LEVEL_FACTORY, CustomLevelFactory::bootstrap)
}