package net.casual.arcade.border

import net.casual.arcade.utils.TickUtils
import net.minecraft.util.Mth
import net.minecraft.world.level.border.BorderChangeListener.DelegateBorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class MovingBorderState(
    private val border: ArcadeBorder,
    private val realDuration: Long,
    private val sizeFrom: Double,
    private val sizeTo: Double
): BorderState {
    private var tickDuration = realDuration / 50.0
    private var ticks = 0

    override fun getMinX(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(border.centerX - getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getMaxX(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(border.centerX + getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getMaxZ(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(border.centerZ + getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getMinZ(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(border.centerZ - getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getSize(): Double {
        val progress = ticks / tickDuration
        return if (progress < 1.0) Mth.lerp(progress, sizeFrom, sizeTo) else sizeTo
    }

    override fun getLerpSpeed(): Double {
        return abs(this.sizeFrom - this.sizeTo) / this.realDuration
    }

    override fun getLerpRemainingTime(): Long {
        val tps = TickUtils.calculateTPS()
        return ((this.tickDuration - this.ticks) / tps * 1000).toLong()
    }

    override fun getLerpTarget(): Double {
        return this.sizeTo
    }

    override fun getStatus(): BorderStatus {
        return if (this.sizeTo < this.sizeFrom) BorderStatus.SHRINKING else BorderStatus.GROWING
    }

    override fun onCenterChange() {

    }



/*    fun getCenter(): Vector2d {
        val progress = ticks / tickDuration
        if (progress >= 1.0) {

            return Vector2d(this.border.getTargetCenterX(), this.border.getTargetCenterX())
        }
        val lerpedX = Mth.lerp(progress, this.border.centerX, this.border.getTargetCenterX())
        val lerpedZ = Mth.lerp(progress, this.border.centerZ, this.border.getTargetCenterZ())
        return Vector2d(lerpedX, lerpedZ)

    }*/

    override fun getCenterX(centerX: Double, targetCenterX: Double): Double {
        val progress = ticks / tickDuration
        return if (progress < 1.0) Mth.lerp(progress, centerX, targetCenterX) else targetCenterX
    }

    override fun getCenterZ(centerZ: Double, targetCenterZ: Double): Double {
        val progress = ticks / tickDuration
        return if (progress < 1.0) Mth.lerp(progress, centerZ, targetCenterZ) else targetCenterZ
    }

    override fun update(): BorderState {
        //TODO: does this work?

        if (this.ticks++ % 20 == 0) {
            // We need to update any listeners
            // Most importantly those that send updates to the client
            // This is because the client logic uses real time
            // So if the tick speed has changed we need to tell the client
            for (listener in this.border.listeners) {
                // We do not want to update DelegateBorderChangeListener
                // This updates borders in other dimensions
                if (listener !is DelegateBorderChangeListener) {
                    listener.onBorderSizeLerping(this.border, this.sizeFrom, this.sizeTo, this.realDuration)


                    listener.onBorderCenterSet(this.border, this.border.centerX, this.border.centerZ)


                }
            }
        }

/*        if (this.border.centerX != this.border.getTargetCenterX() && this.border.centerZ != this.border.getTargetCenterZ()) {
            for (listener in this.border.listeners) {
                //TODO: Experimental
                listener.onBorderCenterSet(this.border, this.border.centerX, this.border.centerZ)
            }
        }*/

        return if (this.ticks >= this.tickDuration) StillBorderState(this.border, this.sizeTo) else this
    }

    override fun getCollisionShape(): VoxelShape {
        return Shapes.join(
            Shapes.INFINITY,
            Shapes.box(
                floor(this.getMinX()), Double.NEGATIVE_INFINITY,
                floor(this.getMinZ()),
                ceil(this.getMaxX()), Double.POSITIVE_INFINITY,
                ceil(this.getMaxZ())
            ),
            BooleanOp.ONLY_FIRST
        )
    }
}