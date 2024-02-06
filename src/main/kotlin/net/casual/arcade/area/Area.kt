package net.casual.arcade.area

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Clearable
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import org.jetbrains.annotations.ApiStatus.NonExtendable
import java.util.*
import java.util.function.Predicate

public interface Area {
    public val level: ServerLevel

    public fun getBoundingBox(): BoundingBox

    public fun getEntityBoundingBox(): AABB {
        val box = this.getBoundingBox()
        return AABB(
            box.minX() - ENTITY_SPACE,
            box.minY() - ENTITY_SPACE,
            box.minZ() - ENTITY_SPACE,
            box.maxX() + ENTITY_SPACE,
            box.maxY() + ENTITY_SPACE,
            box.maxZ() + ENTITY_SPACE
        )
    }

    @NonExtendable
    public fun removeBlocks() {
        val air = Blocks.AIR.defaultBlockState()
        val level = this.level
        BlockPos.betweenClosedStream(this.getBoundingBox()).forEach { pos ->
            val blockEntity = level.getBlockEntity(pos)
            Clearable.tryClear(blockEntity)
            level.setBlock(pos, air, Block.UPDATE_CLIENTS or Block.UPDATE_SUPPRESS_DROPS, 0)
        }
    }

    @NonExtendable
    public fun removeEntities(predicate: Predicate<Entity>) {
        val box = this.getEntityBoundingBox()
        val entities = LinkedList<Entity>()
        for (entity in this.level.getEntitiesOfClass(Entity::class.java, box, predicate)) {
            entities.add(entity)
        }
        for (entity in entities) {
            entity.kill()
        }
    }

    @NonExtendable
    public fun removeAllButPlayers() {
        this.removeBlocks()
        this.removeEntities { it !is ServerPlayer }
    }

    private companion object {
        const val ENTITY_SPACE = 20.0
    }
}