package net.casual.arcade.border.state

public interface CenterBorderState {
    public fun getCenterX(): Double

    public fun getCenterZ(): Double

    public fun update(): CenterBorderState

    public fun getStatus(): CenterBorderStatus
}