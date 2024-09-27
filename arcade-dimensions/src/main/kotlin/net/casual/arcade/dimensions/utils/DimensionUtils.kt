package net.casual.arcade.dimensions.utils

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.mixins.level.MinecraftServerAccessor
import net.casual.arcade.utils.ArcadeUtils
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import org.apache.commons.io.file.PathUtils
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.isDirectory

/**
 * Adds a [CustomLevel] to the server.
 *
 * This registers the level to the server and
 * will fire the [ServerWorldEvents.LOAD] fabric event.
 *
 * Once added, the level will be fully functional and be
 * ticked and can accept players.
 *
 * This will throw if there is a level already added
 * with the same dimension key.
 *
 * @param level The level to add.
 * @return The added level.
 */
public fun MinecraftServer.addCustomLevel(level: CustomLevel): ServerLevel {
    val levels = (this as MinecraftServerAccessor).levels
    if (levels.containsKey(level.dimension())) {
        throw IllegalArgumentException("Tried to load level ${level.dimension().location()} when it was already loaded")
    }
    levels[level.dimension()] = level
    ServerWorldEvents.LOAD.invoker().onWorldLoad(this, level)

    if (level.persistence == LevelPersistence.Persistent) {
        LevelPersistenceTracker.mark(level.dimension())
    }
    return level
}

/**
 * Adds a [CustomLevel] to the server.
 *
 * This registers the level to the server and
 * will fire the [ServerWorldEvents.LOAD] fabric event.
 *
 * Once added, the level will be fully functional and be
 * ticked and can accept players.
 *
 * This will throw if there is a level already added
 * with the same dimension key.
 *
 * @param builder The builder to create the level.
 * @return The added level.
 */
public fun MinecraftServer.addCustomLevel(builder: CustomLevelBuilder): ServerLevel {
    return this.addCustomLevel(builder.build(this))
}

/**
 * Adds a [CustomLevel] to the server.
 *
 * This registers the level to the server and
 * will fire the [ServerWorldEvents.LOAD] fabric event.
 *
 * Once added, the level will be fully functional and be
 * ticked and can accept players.
 *
 * This will throw if there is a level already added
 * with the same dimension key.
 *
 * @param block The method to configure the builder.
 * @return The added level.
 */
public inline fun MinecraftServer.addCustomLevel(block: CustomLevelBuilder.() -> Unit): ServerLevel {
    val builder = CustomLevelBuilder()
    builder.block()
    return this.addCustomLevel(builder)
}

/**
 * Loads a [CustomLevel] from the server.
 *
 * This will try and read the level's dimension data
 * and then re-construct the [CustomLevel].
 * Once the level is re-constructed it is added to the
 * [MinecraftServer].
 *
 * If there is no dimension data or if reading/parsing
 * the dimension data fails this method will return `null`.
 *
 * @param location The location of the level.
 * @return The level, or `null` if it does not exist.
 */
public fun MinecraftServer.loadCustomLevel(location: ResourceLocation): ServerLevel? {
    return this.loadCustomLevel(ResourceKey.create(Registries.DIMENSION, location))
}

/**
 * Loads a [CustomLevel] from the server.
 *
 * This will try and read the level's dimension data
 * and then re-construct the [CustomLevel].
 * Once the level is re-constructed it is added to the
 * [MinecraftServer].
 *
 * If there is no dimension data or if reading/parsing
 * the dimension data fails this method will return `null`.
 *
 * @param key The key of the level.
 * @return The level, or `null` if it does not exist.
 */
public fun MinecraftServer.loadCustomLevel(key: ResourceKey<Level>): ServerLevel? {
    val loaded = this.getLevel(key)
    if (loaded != null) {
        return loaded
    }
    val custom = CustomLevel.read(this, key) ?: return null
    return this.addCustomLevel(custom)
}

/**
 * Loads a [CustomLevel] from the server.
 *
 * This will try and read the level's dimension data
 * and then re-construct the [CustomLevel].
 * Once the level is re-constructed it is added to the
 * [MinecraftServer].
 *
 * If there is no dimension data or if reading/parsing
 * the dimension data fails, this method will add the level
 * instead, using the provided builder method.
 *
 * @param location The location of the level.
 * @param block The method to configure the builder.
 * @return The level.
 */
public inline fun MinecraftServer.loadOrAddCustomLevel(
    location: ResourceLocation,
    block: CustomLevelBuilder.() -> Unit
): ServerLevel {
    return this.loadCustomLevel(location) ?: this.addCustomLevel { dimensionKey(location).block() }
}

/**
 * Loads a [CustomLevel] from the server.
 *
 * This will try and read the level's dimension data
 * and then re-construct the [CustomLevel].
 * Once the level is re-constructed it is added to the
 * [MinecraftServer].
 *
 * If there is no dimension data or if reading/parsing
 * the dimension data fails, this method will add the level
 * instead, using the provided builder method.
 *
 * @param key The key of the level.
 * @param block The method to configure the builder.
 * @return The level.
 */
public inline fun MinecraftServer.loadOrAddCustomLevel(
    key: ResourceKey<Level>,
    block: CustomLevelBuilder.() -> Unit
): ServerLevel {
    return this.loadCustomLevel(key) ?: this.addCustomLevel { dimensionKey(key).block() }
}

/**
 * Removes a [CustomLevel] from the server.
 *
 * This will remove the level from the server and
 * will fire the [ServerWorldEvents.UNLOAD] fabric event.
 *
 * Players should be removed from the level before calling.
 * Any remaining players in the level will be removed and
 * teleported to the overworld, if that fails, then players will
 * be kicked from the server.
 *
 * @param level The level to remove.
 * @param save If the level should try to be saved before closing.
 * @return `true` if the level was removed, `false` otherwise.
 */
public fun MinecraftServer.removeCustomLevel(level: CustomLevel, save: Boolean = true): Boolean {
    if ((this as MinecraftServerAccessor).levels.remove(level.dimension(), level)) {
        LevelPersistenceTracker.unmark(level.dimension())

        level.removePlayers()

        if (save) {
            level.save(null, flush = true, skip = false)
            level.close()
        }
        ServerWorldEvents.UNLOAD.invoker().onWorldUnload(this, level)
        return true
    }
    return false
}

/**
 * Deletes a [CustomLevel] from the server.
 *
 * This will first remove the level with [removeCustomLevel].
 *
 * The level's directory will be deleted after it is removed.
 *
 * @param level The level to delete.
 * @return `true` if the level was deleted, `false` otherwise.
 * @see removeCustomLevel
 */
public fun MinecraftServer.deleteCustomLevel(level: CustomLevel): Boolean {
    if (this.removeCustomLevel(level, false)) {
        val directory = this.getDimensionPath(level.dimension())
        if (directory.isDirectory()) {
            try {
                PathUtils.deleteDirectory(directory)
            } catch (e: IOException) {
                ArcadeUtils.logger.warn("Failed to delete level directory", e)
                PathUtils.deleteOnExit(directory)
            }
        }
        return true
    }
    return false
}

public fun MinecraftServer.getDimensionPath(dimension: ResourceKey<Level>): Path {
    return (this as MinecraftServerAccessor).storage.getDimensionPath(dimension)
}

private fun ServerLevel.removePlayers() {
    val players = this.players()
    if (players.isEmpty()) {
        return
    }

    val overworld = this.server.overworld()
    for (player in players.toList()) {
        val position = player.adjustSpawnLocation(overworld, overworld.sharedSpawnPos).bottomCenter
        player.teleportTo(overworld, position.x, position.y, position.z, 0.0F, 0.0F)
    }

    if (players.isNotEmpty()) {
        ArcadeUtils.logger.warn("Failed to remove players from closing world, kicking players")
        for (player in players.toList()) {
            player.connection.disconnect(Component.literal("Failed to remove you before world closed"))
        }
    }
}