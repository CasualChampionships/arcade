/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer.options

import com.mojang.serialization.Codec
import net.casual.arcade.border.renderer.AxisAlignedDisplayBoundaryRenderer
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.border.utils.BoundaryRegistries
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.Direction
import net.minecraft.core.Registry
import net.minecraft.util.Brightness
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * Provides options for [AxisAlignedDisplayBoundaryRenderer]s.
 */
public interface AxisAlignedModelRenderOptions {
    /**
     * Gets the [Data] for the [shape] and a given [face].
     *
     * @param shape The shape that's being rendered.
     * @param face The face of the boundary that's being rendered.
     * @return The model and brightness data for that boundary face.
     */
    public fun get(shape: BoundaryShape, face: Direction): Data

    /**
     * Provides constant options for [AxisAlignedModelRenderOptions].
     *
     * @param stationary The stationary boundary model.
     * @param shrinking The shrinking boundary model.
     * @param growing The growing boundary model.
     * @param brightness The brightness of the boundary models.
     */
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
        /**
         * Default render options, just render stationary as blue glass,
         * shrinking as red glass, and growing as lime glass.
         */
        public val DEFAULT: AxisAlignedModelRenderOptions = this.register(
            "default",
            Constant(
                ItemStack(Items.LIGHT_BLUE_STAINED_GLASS),
                ItemStack(Items.RED_STAINED_GLASS),
                ItemStack(Items.LIME_STAINED_GLASS),
                Brightness.FULL_BRIGHT
            )
        )

        /**
         * This requires the resource pack [ArcadeResourcePacks.BOUNDARY_SHADER],
         * it uses custom item models and a custom shader to try and simulate
         * the vanilla world border.
         *
         * This is intended for cuboid shaped boundaries, it renders each border face
         * to the correct size and ratio.
         * It provides less precision than [CUBE_SHADER], and only works for boundaries
         * with sizes in the range `[0.5..32760]` and becomes more jittery the larger the size.
         */
        public val CUBOID_SHADER: AxisAlignedModelRenderOptions = this.register(
            "cuboid_shader", AxisAlignedModelCuboidShaderRenderOptions()
        )

        /**
         * This requires the resource pack [ArcadeResourcePacks.BOUNDARY_SHADER],
         * it uses custom item models and a custom shader to try and simulate
         * the vanilla world border.
         *
         * This is intended for cube shaped boundaries, it renders each boundary face as
         * if it were a square, so having non-equal face lengths will result in stretched textures.
         * It provides more precision than [CUBOID_SHADER] however, and will work for all
         * boundaries with sizes in range of the 32 floating point limit.
         */
        public val CUBE_SHADER: AxisAlignedModelRenderOptions = this.register(
            "cube_shader", AxisAlignedModelCubeShaderRenderOptions()
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