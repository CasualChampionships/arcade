/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.data.impl

import net.casual.arcade.dimensions.utils.getDimensionPath
import net.casual.arcade.minigame.data.MinigameData
import net.casual.arcade.minigame.data.MinigameDataType
import net.casual.arcade.minigame.data.MinigameDataProvider
import net.casual.arcade.minigame.data.MinigameDataSet
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.file.ReadableArchive
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.Level
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

/**
 * Implementation of [MinigameData] that provides world files
 * for a [Level].
 *
 * To provide this data a [ReadableArchive] must have a `world/`
 * directory which contains world files, then add `arcade:world`
 * to the `minigame_data.json`.
 *
 * To extract the world you can fetch the data on your [MinigameDataSet]
 * instance and call [extract] providing either the destination
 * path, or more conveniently the dimension key for your dimension.
 *
 * @see MinigameData
 * @see MinigameDataSet
 */
public class MinigameWorldData(
    private val archive: ReadableArchive
): MinigameData {
    override fun type(): MinigameDataType<MinigameWorldData> {
        return TYPE
    }

    /**
     * Extracts the world data to the specified [destination].
     *
     * @param destination The destination to extract to.
     */
    @OptIn(ExperimentalPathApi::class)
    public fun extract(destination: Path) {
        val world = this.archive.resolve(WORLD_DIRECTORY)
        world.copyToRecursively(destination, followLinks = false, overwrite = true)
    }

    /**
     * Extracts the world data for a specified [dimension].
     *
     * It is important that the [dimension] is either unloaded
     * or does not exist yet, otherwise the game will simply
     * overwrite the extracted files on the next game save.
     *
     * @param server The server to extract to.
     * @param dimension The target dimension.
     */
    public fun extract(server: MinecraftServer, dimension: ResourceKey<Level>) {
        this.extract(server.getDimensionPath(dimension).createDirectories())
    }

    public companion object: MinigameDataProvider<MinigameWorldData> {
        private const val WORLD_DIRECTORY = "world"

        public val TYPE: MinigameDataType<MinigameWorldData> = MinigameDataType(arcade("world"))

        override val type: MinigameDataType<MinigameWorldData> = TYPE

        override fun get(archive: ReadableArchive, server: MinecraftServer): MinigameWorldData {
            if (archive.resolve(WORLD_DIRECTORY).notExists()) {
                throw IllegalArgumentException("Cannot create world data, no world directory exists!")
            }
            return MinigameWorldData(archive)
        }
    }
}
