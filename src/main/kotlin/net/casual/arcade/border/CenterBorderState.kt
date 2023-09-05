package net.casual.arcade.border

import net.minecraft.world.level.border.BorderStatus

interface CenterBorderState {

    fun getCenterX(): Double
    fun getCenterZ(): Double

    fun update(): CenterBorderState
    fun getStatus(): BorderStatus
}