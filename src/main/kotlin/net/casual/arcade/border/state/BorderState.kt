package net.casual.arcade.border.state

import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.phys.shapes.VoxelShape

public interface BorderState {
    public fun getMinX(): Double

    public fun getMaxX(): Double

    public fun getMinZ(): Double

    public fun getMaxZ(): Double


    //Diameter
    public fun getSize(): Double

    public fun getLerpSpeed(): Double

    public fun getLerpRemainingTime(): Long

    public fun getLerpTarget(): Double

    public fun getStatus(): BorderStatus

    public fun onCenterChange()

    public fun update(): BorderState

    public fun getCollisionShape(): VoxelShape
}