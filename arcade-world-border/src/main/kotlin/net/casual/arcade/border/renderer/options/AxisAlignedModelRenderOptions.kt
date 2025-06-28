/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer.options

import com.mojang.serialization.Codec
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.border.utils.BoundaryRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.util.Brightness
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

public interface AxisAlignedModelRenderOptions {
    public fun get(shape: BoundaryShape, face: Direction): Data

    public class Constant(
        public val stationary: ItemStack,
        public val shrinking: ItemStack,
        public val growing: ItemStack,
        public val brightness: Brightness
    ): AxisAlignedModelRenderOptions {
        override fun get(shape: BoundaryShape, face: Direction): Data {
            val model = shape.getStatus().choose(this.stationary, this.shrinking, this.growing)
            return Data(model, this.brightness)
        }
    }

    public data class Data(val model: ItemStack, val brightness: Brightness)

    public companion object {
        public val DEFAULT: AxisAlignedModelRenderOptions = this.register(
            "default",
            Constant(
                ItemStack(Items.LIGHT_BLUE_STAINED_GLASS),
                ItemStack(Items.RED_STAINED_GLASS),
                ItemStack(Items.LIME_STAINED_GLASS),
                Brightness.FULL_BRIGHT
            )
        )
        public val CUBOID_SHADER: AxisAlignedModelRenderOptions = this.register(
            "cuboid_shader", AxisAlignedModelCuboidShaderRenderOptions
        )
        public val CUBE_SHADER: AxisAlignedModelRenderOptions = this.register(
            "cube_shader", AxisAlignedModelCubeShaderRenderOptions
        )

        public val CODEC: Codec<AxisAlignedModelRenderOptions> = Codec.lazyInitialized {
            BoundaryRegistries.MODEL_BOUNDARY_RENDER_OPTIONS.byNameCodec()
        }

        internal fun bootstrap() {

        }

        private fun register(path: String, options: AxisAlignedModelRenderOptions): AxisAlignedModelRenderOptions {
            return Registry.register(BoundaryRegistries.MODEL_BOUNDARY_RENDER_OPTIONS, ArcadeUtils.id(path), options)
        }
    }
}