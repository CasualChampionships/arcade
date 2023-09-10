package net.casual.arcade.area

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox

class BoxedArea(
    private val center: Vec3i,
    private val radius: Int,
    private val height: Int,
    override val level: ServerLevel,
    private val block: Block = Blocks.BARRIER
): PlaceableArea {
    override fun place(): Boolean {
        val box = this.getBoundingBox()
        val barrier = this.block.defaultBlockState()
        BlockPos.betweenClosedStream(box).filter { p ->
            p.x == box.minX() || p.x == box.maxX() || p.y == box.minY() || p.y == box.maxY() || p.z == box.minZ() || p.z == box.maxZ()
        }.forEach { pos ->
            this.level.setBlock(pos, barrier, Block.UPDATE_CLIENTS)
        }
        return true
    }

    override fun getBoundingBox(): BoundingBox {
        return BoundingBox(
            this.center.x - this.radius,
            this.center.y,
            this.center.z - this.radius,
            this.center.x + this.radius,
            this.center.y + this.height,
            this.center.z + this.radius
        )
    }
}