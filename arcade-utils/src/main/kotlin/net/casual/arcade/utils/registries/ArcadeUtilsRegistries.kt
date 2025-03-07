/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.registries

import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.math.location.providers.LocationProvider
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import org.jetbrains.annotations.ApiStatus.Internal

public object ArcadeUtilsRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    public val LOCATION_PROVIDER: ResourceKey<Registry<MapCodec<out LocationProvider>>> = create("location_provider")
}

public object ArcadeUtilsRegistries: RegistrySupplier() {
    public val LOCATION_PROVIDER: Registry<MapCodec<out LocationProvider>> = create(ArcadeUtilsRegistryKeys.LOCATION_PROVIDER, LocationProvider::bootstrap)

    @Internal
    public fun init() {
        this.load()
    }
}