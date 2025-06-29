/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.boundary.renderer.BoundaryRenderer
import net.casual.arcade.boundary.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.boundary.renderer.options.ParticleRenderOptions
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object BoundaryRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    public val BOUNDARY_RENDERER_FACTOR: ResourceKey<Registry<MapCodec<out BoundaryRenderer.Factory>>> = this.create("border_renderer_factory")
    public val BOUNDARY_SHAPE: ResourceKey<Registry<MapCodec<out BoundaryShape>>> = this.create("border_shape")
    public val PARTICLE_RENDER_OPTIONS: ResourceKey<Registry<ParticleRenderOptions>> = this.create("particle_render_options")
    public val MODEL_RENDER_OPTIONS: ResourceKey<Registry<AxisAlignedModelRenderOptions>> = this.create("model_render_options")
}

public object BoundaryRegistries: RegistrySupplier() {
    public val BOUNDARY_RENDERER_FACTORY: Registry<MapCodec<out BoundaryRenderer.Factory>> = this.create(BoundaryRegistryKeys.BOUNDARY_RENDERER_FACTOR, BoundaryRenderer.Factory::bootstrap)
    public val BOUNDARY_SHAPE: Registry<MapCodec<out BoundaryShape>> = this.create(BoundaryRegistryKeys.BOUNDARY_SHAPE, BoundaryShape::bootstrap)
    public val PARTICLE_RENDER_OPTIONS: Registry<ParticleRenderOptions> = this.create(BoundaryRegistryKeys.PARTICLE_RENDER_OPTIONS) { ParticleRenderOptions.bootstrap() }
    public val MODEL_BOUNDARY_RENDER_OPTIONS: Registry<AxisAlignedModelRenderOptions> = this.create(BoundaryRegistryKeys.MODEL_RENDER_OPTIONS) { AxisAlignedModelRenderOptions.bootstrap() }
}