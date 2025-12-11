/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data.module

import net.casual.arcade.dimensions.utils.getDimensionPath
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.file.ReadableArchive
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

public class MinigameWorldData(
    private val archive: ReadableArchive
): MinigameDataModule {
    @OptIn(ExperimentalPathApi::class)
    public fun extract(destination: Path) {
        val world = this.archive.resolve(WORLD_DIRECTORY)
        world.copyToRecursively(destination, followLinks = false, overwrite = true)
    }

    public fun extract(server: MinecraftServer, dimension: ResourceKey<Level>) {
        this.extract(server.getDimensionPath(dimension).createDirectories())
    }

    public companion object: MinigameDataModule.Provider {
        private const val WORLD_DIRECTORY = "world"

        override val id: Identifier = IdentifierUtils.arcade("world")

        override fun get(archive: ReadableArchive, server: MinecraftServer): MinigameWorldData {
            if (archive.resolve(WORLD_DIRECTORY).notExists()) {
                throw IllegalArgumentException("Cannot create world data module, no world directory exists!")
            }
            return MinigameWorldData(archive)
        }
    }
}