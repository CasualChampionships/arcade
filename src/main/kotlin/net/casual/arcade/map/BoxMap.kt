package net.casual.arcade.map

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox

class BoxMap(
    val center: Vec3i,
    val radius: Int,
    val height: Int,
    override val level: ServerLevel,
    val block: Block = Blocks.BARRIER
): PlaceableMap {
    override fun place() {
        val box = this.getBoundingBox()
        val barrier = this.block.defaultBlockState()
        BlockPos.betweenClosedStream(box).filter { p ->
            p.x == box.minX() || p.x == box.maxX() || p.y == box.minY() || p.y == box.maxY() || p.z == box.minZ() || p.z == box.maxZ()
        }.forEach { pos ->
            this.level.setBlock(pos, barrier, 3)
        }
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