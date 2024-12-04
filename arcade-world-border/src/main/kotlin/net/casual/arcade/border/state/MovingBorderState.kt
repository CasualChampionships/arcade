package net.casual.arcade.border.state

import net.casual.arcade.border.CustomBorder
import net.casual.arcade.utils.ServerUtils
import net.casual.arcade.utils.calculateTPS
import net.minecraft.util.Mth
import net.minecraft.world.level.border.BorderChangeListener.DelegateBorderChangeListener
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

public class MovingBorderState(
    private val border: CustomBorder,
    private val realDuration: Long,
    private val sizeFrom: Double,
    private val sizeTo: Double
): BorderState {
    private var tickDuration = realDuration / 50.0
    private var ticks = 0

    override fun getMinX(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(this.border.centerX - getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getMaxX(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(this.border.centerX + getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getMaxZ(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(this.border.centerZ + getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getMinZ(): Double {
        val maxSize = this.border.absoluteMaxSize
        return Mth.clamp(this.border.centerZ - getSize() / 2.0, -maxSize.toDouble(), maxSize.toDouble())
    }

    override fun getSize(): Double {
        val progress = this.ticks / this.tickDuration
        return if (progress < 1.0) Mth.lerp(progress, this.sizeFrom, this.sizeTo) else this.sizeTo
    }

    override fun getLerpSpeed(): Double {
        return abs(this.sizeFrom - this.sizeTo) / this.realDuration
    }

    override fun getLerpRemainingTime(): Long {
        val tps = ServerUtils.getServerOrNull()?.calculateTPS() ?: 20.0F
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

    override fun update(): BorderState {
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
                }
            }
        }

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