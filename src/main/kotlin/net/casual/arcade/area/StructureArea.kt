package net.casual.arcade.area

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate

public class StructureArea(
    private val structure: StructureTemplate,
    private val centre: Vec3i,
    override val level: ServerLevel
): PlaceableArea {
    private val box by lazy(this::calculateBoundingBox)

    override fun place(): Boolean {
        val dimensions = this.structure.size
        val halfX = dimensions.x / 2 + 1
        val halfY = dimensions.y / 2 + 1
        val halfZ = dimensions.z / 2 + 1
        val corner = BlockPos(this.centre.x - halfX, this.centre.y - halfY, this.centre.z - halfZ)
        return this.structure.placeInWorld(
            this.level,
            corner,
            corner,
            StructurePlaceSettings().setKnownShape(true),
            RandomSource.create(),
            Block.UPDATE_CLIENTS
        )
    }

    override fun getBoundingBox(): BoundingBox {
        return this.box
    }

    private fun calculateBoundingBox(): BoundingBox {
        val dimensions = this.structure.size
        val halfX = dimensions.x / 2 + 1
        val halfY = dimensions.y / 2 + 1
        val halfZ = dimensions.z / 2 + 1
        return BoundingBox(
            this.centre.x - halfX,
            this.centre.y - halfY,
            this.centre.z - halfZ,
            this.centre.x + halfX,
            this.centre.y + halfY,
            this.centre.z + halfZ
        )
    }
}