package net.casual.arcade.border

interface CenterBorderState {

    fun getCenterX(): Double
    fun getCenterZ(): Double

    fun update(): CenterBorderState
}