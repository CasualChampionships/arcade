/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data.module


import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.file.ReadableArchive
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.notExists

public class MinigameWorldData(
    private val archive: ReadableArchive
): MinigameDataModule {
    @OptIn(ExperimentalPathApi::class)
    public fun extract(destination: Path) {
        val world = this.archive.resolve(WORLD_DIRECTORY)
        world.copyToRecursively(destination, followLinks = false, overwrite = true)
    }

    public companion object: MinigameDataModule.Provider {
        private const val WORLD_DIRECTORY = "world"

        override val id: ResourceLocation = ResourceUtils.arcade("world")

        override fun get(archive: ReadableArchive, server: MinecraftServer): MinigameDataModule {
            if (archive.resolve(WORLD_DIRECTORY).notExists()) {
                throw IllegalArgumentException("Cannot create world data module, no world directory exists!")
            }
            return MinigameWorldData(archive)
        }
    }
}