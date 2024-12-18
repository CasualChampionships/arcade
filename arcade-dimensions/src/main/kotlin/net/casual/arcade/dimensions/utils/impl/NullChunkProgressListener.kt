/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.utils.impl

import net.minecraft.server.level.progress.ChunkProgressListener
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.chunk.status.ChunkStatus
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
internal object NullChunkProgressListener: ChunkProgressListener {
    override fun updateSpawnPos(center: ChunkPos) {

    }

    override fun onStatusChange(chunkPos: ChunkPos, chunkStatus: ChunkStatus?) {

    }

    override fun start() {

    }

    override fun stop() {

    }
}