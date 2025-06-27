/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer.options

import com.mojang.serialization.Codec
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.border.shape.BoundaryShape.Status
import net.casual.arcade.border.utils.BoundaryRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

public interface AxisAlignedModelRenderOptions {
    public fun get(shape: BoundaryShape, face: Direction): ItemStack

    public class Constant(
        public val stationary: ItemStack,
        public val shrinking: ItemStack,
        public val growing: ItemStack
    ): AxisAlignedModelRenderOptions {
        override fun get(shape: BoundaryShape, face: Direction): ItemStack {
            return shape.getStatus().choose(this.stationary, this.shrinking, this.growing)
        }
    }

    public companion object {
        public val DEFAULT: AxisAlignedModelRenderOptions = this.register(
            "default",
            Constant(
                ItemStack(Items.LIGHT_BLUE_STAINED_GLASS),
                ItemStack(Items.RED_STAINED_GLASS),
                ItemStack(Items.LIME_STAINED_GLASS)
            )
        )
        public val SHADER: AxisAlignedModelRenderOptions = this.register(
            "shader", AxisAlignedModelShaderRenderOptions
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