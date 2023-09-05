package net.casual.arcade.border

import net.minecraft.util.Mth
import net.minecraft.world.level.border.BorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import org.joml.Vector2d

class MovingCenterBorderState(
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
        val progress = this.ticks / tickDuration
        return if (progress < 1.0) Mth.lerp(progress, this.centerX, this.targetCenterX) else this.targetCenterX
    }

    override fun getCenterZ(): Double {
        val progress = this.ticks / tickDuration
        return if (progress < 1.0) Mth.lerp(progress, this.centerZ, this.targetCenterZ) else this.targetCenterZ

    }

    override fun update(): CenterBorderState {
        this.ticks++
            // We need to update any listeners
            // Most importantly those that send updates to the client
            // This is because the client logic uses real time
            // So if the tick speed has changed we need to tell the client
            for (listener in this.border.listeners) {
                // We do not want to update DelegateBorderChangeListener
                // This updates borders in other dimensions
                if (listener !is BorderChangeListener.DelegateBorderChangeListener) {
                    listener.onBorderCenterSet(this.border, this.getCenterX(), this.getCenterZ())

                }
            }
        return if (this.ticks >= this.tickDuration) StillCenterBorderState(this.targetCenterX, this.targetCenterZ) else this
    }

    override fun getStatus(): BorderStatus {
        return if ((this.ticks / tickDuration) < 1.0) BorderStatus.SHRINKING else BorderStatus.STATIONARY
    }


}