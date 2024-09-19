package net.casual.arcade.dimensions.border.state

public interface CenterBorderState {
    public fun getCenterX(): Double

    public fun getCenterZ(): Double

    public fun getTargetCenterX(): Double

    public fun getTargetCenterZ(): Double

    public fun getLerpRemainingTime(): Long

    public fun update(): CenterBorderState

    public fun getStatus(): CenterBorderStatus
}