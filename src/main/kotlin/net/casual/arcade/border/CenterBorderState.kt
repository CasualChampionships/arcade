package net.casual.arcade.border

import net.minecraft.world.level.border.BorderStatus

interface CenterBorderState {
    fun update(): CenterBorderState


    fun getLerpSpeed(): Double

    fun getLerpRemainingTime(): Long

    fun getLerpTarget(): Double

    fun getStatus(): BorderStatus

    fun onCenterChange()
}