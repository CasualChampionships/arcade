package net.casual.arcade.border.state

import net.casual.arcade.border.ArcadeBorder
import net.minecraft.util.Mth
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.math.ceil
import kotlin.math.floor

public class StillBorderState(
    private val border: ArcadeBorder,
    private var size: Double
): BorderState {
    private var minX: Double = 0.0
    private var minZ: Double = 0.0
    private var maxX: Double = 0.0
    private var maxZ: Double = 0.0

    private lateinit var shape: VoxelShape

    init {
        this.updateShape()
    }

    override fun getMinX(): Double {
        return this.minX
    }

    override fun getMaxX(): Double {
        return this.maxX
    }

    override fun getMinZ(): Double {
        return this.minZ
    }

    override fun getMaxZ(): Double {
        return this.maxZ
    }

    override fun getSize(): Double {
        return this.size
    }

    override fun getStatus(): BorderStatus {
        return BorderStatus.STATIONARY
    }

    override fun getLerpSpeed(): Double {
        return 0.0
    }

    override fun getLerpRemainingTime(): Long {
        return 0L
    }

    override fun getLerpTarget(): Double {
        return this.size
    }

    override fun onCenterChange() {
        updateShape()
    }

    override fun update(): BorderState {
        return this
    }

    override fun getCollisionShape(): VoxelShape {
        return this.shape
    }

    private fun updateShape() {
        this.minX = Mth.clamp(
            this.border.centerX - size / 2.0,
            -this.border.absoluteMaxSize.toDouble(),
            this.border.absoluteMaxSize.toDouble()
        )
        this.minZ = Mth.clamp(
            this.border.centerZ - size / 2.0,
            -this.border.absoluteMaxSize.toDouble(),
            this.border.absoluteMaxSize.toDouble()
        )
        this.maxX = Mth.clamp(
            this.border.centerX + size / 2.0,
            -this.border.absoluteMaxSize.toDouble(),
            this.border.absoluteMaxSize.toDouble()
        )
        this.maxZ = Mth.clamp(
            this.border.centerZ + size / 2.0,
            -this.border.absoluteMaxSize.toDouble(),
            this.border.absoluteMaxSize.toDouble()
        )
        this.shape = Shapes.join(
            Shapes.INFINITY,
            Shapes.box(
                floor(this.getMinX()),
                Double.NEGATIVE_INFINITY,
                floor(this.getMinZ()),
                ceil(this.getMaxX()),
                Double.POSITIVE_INFINITY,
                ceil(this.getMaxZ())
            ),
            BooleanOp.ONLY_FIRST
        )
    }
}