package net.casual.arcade.border.state

interface CenterBorderState {
    fun getCenterX(): Double

    fun getCenterZ(): Double

    fun update(): CenterBorderState

    fun getStatus(): CenterBorderStatus
}