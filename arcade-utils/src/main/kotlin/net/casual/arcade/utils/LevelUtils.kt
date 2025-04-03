package net.casual.arcade.utils

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.chunk.status.ChunkStatus

public inline fun <reified T: BlockEntity> ServerLevel.getBlockEntitiesOfType(positions: Iterable<ChunkPos>): List<T> {
    val list = ArrayList<T>()
    this.forEachBlockEntityOfType<T>(positions) { _, entity -> list.add(entity) }
    return list
}

public inline fun <reified T: BlockEntity> ServerLevel.forEachBlockEntityOfType(
    positions: Iterable<ChunkPos>,
    consumer: (BlockPos, T) -> Unit
) {
    for (position in positions) {
        val chunk = this.getChunk(position.x, position.z, ChunkStatus.FULL, false)
        if (chunk is LevelChunk) {
            for ((pos, entity) in chunk.blockEntities) {
                if (entity is T) {
                    consumer.invoke(pos, entity)
                }
            }
        }
    }
}
