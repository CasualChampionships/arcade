/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.border.renderer.BoundaryRenderer
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object BorderRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    public val BORDER_RENDERER: ResourceKey<Registry<MapCodec<out BoundaryRenderer>>> = this.create("border_renderer")
    public val BORDER_SHAPE: ResourceKey<Registry<MapCodec<out BoundaryShape>>> = this.create("border_shape")
}

public object BorderRegistries: RegistrySupplier() {
    public val BOUNDARY_RENDERER: Registry<MapCodec<out BoundaryRenderer>> = this.create(BorderRegistryKeys.BORDER_RENDERER, BoundaryRenderer::bootstrap)
    public val BOUNDARY_SHAPE: Registry<MapCodec<out BoundaryShape>> = this.create(BorderRegistryKeys.BORDER_SHAPE, BoundaryShape::bootstrap)
}