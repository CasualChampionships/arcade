package net.casualuhc.arcade.border

import net.casualuhc.arcade.utils.TickUtils
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
    private val from: Double,
    private val to: Double
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
        return if (progress < 1.0) Mth.lerp(progress, from, to) else to
    }

    override fun getLerpSpeed(): Double {
        return abs(this.from - this.to) / this.realDuration
    }

    override fun getLerpRemainingTime(): Long {
        val tps = TickUtils.calculateTPS()
        return ((this.tickDuration - this.ticks) / tps * 1000).toLong()
    }

    override fun getLerpTarget(): Double {
        return this.to
    }

    override fun getStatus(): BorderStatus {
        return if (this.to < this.from) BorderStatus.SHRINKING else BorderStatus.GROWING
    }

    override fun onCenterChange() {

    }

    override fun update(): BorderState {
        if (ticks++ % 20 == 0) {
            // We need to update any listeners
            // Most importantly those that send updates to the client
            // This is because the client logic uses real time
            // So if the tick speed has changed we need to tell the client
            for (listener in this.border.listeners) {
                // We do not want to update DelegateBorderChangeListener
                // This updates borders in other dimensions
                if (listener !is DelegateBorderChangeListener) {
                    listener.onBorderSizeLerping(this.border, this.from, this.to, this.realDuration)
                }
            }
        }
        return if (this.ticks >= this.tickDuration) StillBorderState(this.border, this.to) else this
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