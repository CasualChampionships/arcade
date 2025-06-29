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
import kotlin.math.floor
import kotlin.math.log2

public class AxisAlignedModelCuboidShaderRenderOptions: AxisAlignedModelRenderOptions {
    private val stationary = ArcadeUtils.id("boundary/cuboid_stationary")
    private val shrinking = ArcadeUtils.id("boundary/cuboid_shrinking")
    private val growing = ArcadeUtils.id("boundary/cuboid_growing")

    override fun get(shape: BoundaryShape, face: Direction): AxisAlignedModelRenderOptions.Data {
        val stack = ItemStack(Items.POPPED_CHORUS_FRUIT)
        val model = shape.getStatus().choose(this.stationary, this.shrinking, this.growing)
        stack.set(DataComponents.ITEM_MODEL, model)
        val size = shape.size()
        val color = when (face.axis!!) {
            Direction.Axis.X -> pack(size.z / 2, size.y / 2)
            Direction.Axis.Y -> pack(size.x / 2, size.z / 2)
            Direction.Axis.Z -> pack(size.x / 2, size.y / 2)
        }
        stack.set(DataComponents.DYED_COLOR, DyedItemColor(color.toInt()))
        val light = (color shr 24).toInt()
        val brightness = Brightness(light % 16, light / 16)
        return AxisAlignedModelRenderOptions.Data(stack, brightness)
    }

    private fun pack(a: Double, b: Double): UInt {
        val high = castToHalfBits(a).toUInt()
        val low = castToHalfBits(b).toUInt()
        return (high shl 16) or low
    }

    private fun castToHalfBits(value: Double): UShort {
        val clamped = value.coerceIn(0.5, 32760.0)

        val exponent = (floor(log2(clamped)) + 1).toInt()
        val scale = 1 shl (exponent - 1)
        val normalized = clamped / scale

        val mantissa = ((normalized - 1.0) * 4096.0).toInt().coerceIn(0, 0xFFF)
        val bits = ((exponent and 0xF) shl 12) or (mantissa and 0xFFF)
        return bits.toUShort()
    }
}