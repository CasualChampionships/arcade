package net.casual.arcade.border.state

import net.casual.arcade.border.ArcadeBorder
import net.casual.arcade.utils.TickUtils
import net.minecraft.util.Mth

public class MovingCenterBorderState(
    private val border: ArcadeBorder,
    private val centerX: Double,
    private val centerZ: Double,
    private val targetCenterX: Double,
    private val targetCenterZ: Double,
    realDuration: Long,
): CenterBorderState {
    private val tickDuration = realDuration / 50.0
    private var ticks = 0

    override fun getCenterX(): Double {
        val progress = this.ticks / this.tickDuration
        return if (progress < 1.0) Mth.lerp(progress, this.centerX, this.targetCenterX) else this.targetCenterX
    }

    override fun getCenterZ(): Double {
        val progress = this.ticks / this.tickDuration
        return if (progress < 1.0) Mth.lerp(progress, this.centerZ, this.targetCenterZ) else this.targetCenterZ
    }

    override fun getTargetCenterX(): Double {
        return this.targetCenterX
    }

    override fun getTargetCenterZ(): Double {
        return this.targetCenterZ
    }

    override fun getLerpRemainingTime(): Long {
        val tps = TickUtils.calculateTPS()
        return ((this.tickDuration - this.ticks) / tps * 1000).toLong()
    }

    override fun update(): CenterBorderState {
        this.ticks++

        this.border.changeCenter(this.getCenterX(), this.getCenterZ())
        return if (this.ticks >= this.tickDuration) StillCenterBorderState(this.targetCenterX, this.targetCenterZ) else this
    }

    override fun getStatus(): CenterBorderStatus {
        return if ((this.ticks / this.tickDuration) < 1.0) CenterBorderStatus.MOVING else CenterBorderStatus.STATIONARY
    }
}