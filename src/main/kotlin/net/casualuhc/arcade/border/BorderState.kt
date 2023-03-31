package net.casualuhc.arcade.border

import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.phys.shapes.VoxelShape

interface BorderState {
    fun getMinX(): Double

    fun getMaxX(): Double

    fun getMinZ(): Double

    fun getMaxZ(): Double

    fun getSize(): Double

    fun getLerpSpeed(): Double

    fun getLerpRemainingTime(): Long

    fun getLerpTarget(): Double

    fun getStatus(): BorderStatus

    fun onCenterChange()

    fun update(): BorderState

    fun getCollisionShape(): VoxelShape
}