/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.utils

import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.config.PrepareSpawnTask
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.gamerules.GameRules

internal object TestPlayerSpawnPreloader {
    private var prepared: ChunkPos? = null

    fun prepare(server: MinecraftServer) {
        val respawn = server.worldData.overworldData().respawnData
        val center = ChunkPos.containing(respawn.pos())
        if (this.prepared == center) {
            return
        }

        val level = server.getLevel(respawn.dimension()) ?: server.overworld()
        level.gameRules.set(GameRules.RESPAWN_RADIUS, 0, server)

        val radius = PrepareSpawnTask.PREPARE_CHUNK_RADIUS
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                level.getChunk(center.x + x, center.z + z)
            }
        }

        this.prepared = center
    }
}
