/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer.options

import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.DyedItemColor
import kotlin.math.ceil

public object AxisAlignedModelShaderRenderOptions: AxisAlignedModelRenderOptions {
    private val stationary = ArcadeUtils.id("boundary/stationary")
    private val shrinking = ArcadeUtils.id("boundary/shrinking")
    private val growing = ArcadeUtils.id("boundary/growing")

    override fun get(shape: BoundaryShape, face: Direction): ItemStack {
        val stack = ItemStack(Items.POPPED_CHORUS_FRUIT)
        val model = shape.getStatus().choose(this.stationary, this.shrinking, this.growing)
        stack.set(DataComponents.ITEM_MODEL, model)
        val size = shape.size()
        val color = when (face.axis!!) {
            Direction.Axis.X -> pack(size.z, size.y)
            Direction.Axis.Y -> pack(size.x, size.z)
            Direction.Axis.Z -> pack(size.x, size.y)
        }
        stack.set(DataComponents.DYED_COLOR, DyedItemColor(color.toInt()))
        return stack
    }

    private fun pack(high: Double, low: Double): UInt {
        return pack(ceil(high).toUInt(), ceil(low).toUInt())
    }

    private fun pack(high: UInt, low: UInt): UInt {
        return (high shl 16) or (low and 0xFFFFu)
    }
}