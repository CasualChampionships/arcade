/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer.options

import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.util.Brightness
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.DyedItemColor
import kotlin.math.ceil

public class AxisAlignedModelCubeShaderRenderOptions: AxisAlignedModelRenderOptions {
    private val stationary = ArcadeUtils.id("boundary/cube_stationary")
    private val shrinking = ArcadeUtils.id("boundary/cube_shrinking")
    private val growing = ArcadeUtils.id("boundary/cube_growing")

    override fun get(shape: BoundaryShape, face: Direction): AxisAlignedModelRenderOptions.Data {
        val stack = ItemStack(Items.POPPED_CHORUS_FRUIT)
        val model = shape.getStatus().choose(this.stationary, this.shrinking, this.growing)
        stack.set(DataComponents.ITEM_MODEL, model)
        // We encode the width of the border as the bits of the float.
        val size = shape.size().x.toFloat().toBits()
        stack.set(DataComponents.DYED_COLOR, DyedItemColor(size))
        val light = size shr 24
        val brightness = Brightness((light % 16), (light / 16))
        return AxisAlignedModelRenderOptions.Data(stack, brightness)
    }
}