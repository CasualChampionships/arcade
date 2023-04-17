package net.casualuhc.arcade.map

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Clearable
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import java.util.*
import java.util.function.Predicate

interface PlaceableMap {
    val level: ServerLevel

    fun place()

    fun getBoundingBox(): BoundingBox

    fun remove() {
        val air = Blocks.AIR.defaultBlockState()
        val level = this.level
        BlockPos.betweenClosedStream(this.getBoundingBox()).forEach { pos ->
            val blockEntity = level.getBlockEntity(pos)
            Clearable.tryClear(blockEntity)
            level.setBlock(pos, air, Block.UPDATE_CLIENTS or Block.UPDATE_SUPPRESS_DROPS, 0)
        }
    }

    fun removeEntities(predicate: Predicate<Entity>) {
        val box = this.getEntityBoundingBox()
        val entities = LinkedList<Entity>()
        for (entity in level.allEntities) {
            if (box.contains(entity.position()) && predicate.test(entity)) {
                entities.add(entity)
            }
        }
        for (entity in entities) {
            entity.kill()
        }
    }

    fun getEntityBoundingBox(): AABB {
        val lobbyBox = this.getBoundingBox()
        return AABB(
            lobbyBox.minX() - ENTITY_SPACE,
            lobbyBox.minY() - ENTITY_SPACE,
            lobbyBox.minZ() - ENTITY_SPACE,
            lobbyBox.maxX() + ENTITY_SPACE,
            lobbyBox.maxY() + ENTITY_SPACE,
            lobbyBox.maxZ() + ENTITY_SPACE
        )
    }

    companion object {
        private const val ENTITY_SPACE = 20.0
    }
}