package net.casual.arcade.border

import net.minecraft.world.level.border.BorderStatus

class StillCenterBorderState(
    private var centerX: Double,
    private var centerZ: Double
): CenterBorderState {


    override fun getCenterX(): Double {
        return centerX;
    }

    override fun getCenterZ(): Double {
        return centerZ
    }

    override fun update(): CenterBorderState {
        return this
    }

    override fun getStatus(): BorderStatus {
        return BorderStatus.STATIONARY
    }
}