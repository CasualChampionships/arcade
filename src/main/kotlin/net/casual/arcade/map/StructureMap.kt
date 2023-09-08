package net.casual.arcade.map

import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate

class StructureMap(
    val lobby: StructureTemplate,
    val centre: Vec3i,
    override val level: ServerLevel
): PlaceableMap {
    override fun place() {
        val dimensions = this.lobby.size
        val halfX = dimensions.x / 2 + 1
        val halfY = dimensions.y / 2 + 1
        val halfZ = dimensions.z / 2 + 1
        val corner = BlockPos(this.centre.x - halfX, this.centre.y - halfY, this.centre.z - halfZ)
        this.lobby.placeInWorld(this.level, corner, corner, StructurePlaceSettings(), RandomSource.create(), 3)
    }

    override fun getBoundingBox(): BoundingBox {
        val dimensions = this.lobby.size
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