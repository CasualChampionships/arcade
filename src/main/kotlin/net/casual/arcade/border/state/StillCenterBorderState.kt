package net.casual.arcade.border.state

class StillCenterBorderState(
    private var centerX: Double,
    private var centerZ: Double
): CenterBorderState {
    override fun getCenterX(): Double {
        return this.centerX;
    }

    override fun getCenterZ(): Double {
        return this.centerZ
    }

    override fun update(): CenterBorderState {
        return this
    }

    override fun getStatus(): CenterBorderStatus {
        return CenterBorderStatus.STATIONARY
    }
}