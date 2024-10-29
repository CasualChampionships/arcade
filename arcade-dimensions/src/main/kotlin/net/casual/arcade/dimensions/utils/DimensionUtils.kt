package net.casual.arcade.dimensions.utils

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules
import net.casual.arcade.dimensions.level.spawner.extension.LevelCustomMobSpawningExtension
import net.casual.arcade.dimensions.mixins.level.MinecraftServerAccessor
import net.casual.arcade.extensions.event.LevelExtensionEvent.Companion.getExtension
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.teleportTo
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
 * This method may be called if the [level] instance has
 * already been registered, it will simply just return the
 * instance. If there is a different level instance
 * already added with the same dimension key, then this
 * method will throw an exception.
 *
 * @param level The level to add.
 * @return The added level.
 */
public fun MinecraftServer.addCustomLevel(level: CustomLevel): ServerLevel {
    val levels = (this as MinecraftServerAccessor).levels
    val dimension = level.dimension()
    if (levels.containsKey(dimension)) {
        if (levels[dimension] !== level) {
            throw IllegalArgumentException(
                "Tried to add level ${dimension.location()} when a different level is registered with that key"
            )
        }
        return level
    }
    levels[dimension] = level

    level.onLoad()
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
 * Returns whether the server has the exact [level] instance.
 *
 * This checks the [level] reference *not* the dimension key.
 * It is possible for the server to have a different level
 * instance under the same dimension key, in which case
 * this method will return `false`.
 *
 * If you want to check whether a level with a specific
 * dimension key is loaded, you can do:
 * ```
 * server.levelKeys().contains(dimensionKey)
 * ```
 *
 * @param level The level to check for.
 * @return Whether the server has the specified level.
 */
public fun MinecraftServer.hasCustomLevel(level: CustomLevel): Boolean {
    return this.getLevel(level.dimension()) === level
}

/**
 * Removes a [CustomLevel] from the server.
 *
 * This will remove the level from the server and
 * will fire the [ServerWorldEvents.UNLOAD] fabric event.
 *
 * If the [CustomLevel.persistence] is [LevelPersistence.Temporary]
 * then the level will be deleted instead, equivalent of calling
 * [deleteCustomLevel].
 *
 * Players should be removed from the level before calling.
 * Any remaining players in the level will be removed and
 * teleported to the overworld, if that fails, then players will
 * be kicked from the server.
 *
 * @param level The level to remove.
 * @return `true` if the level was removed, `false` otherwise.
 */
public fun MinecraftServer.removeCustomLevel(level: CustomLevel): Boolean {
    return if (level.persistence.shouldSave()) {
        this.unloadCustomLevel(level, true)
    } else {
        this.deleteCustomLevel(level)
    }
}

/**
 * Deletes a [CustomLevel] from the server.
 *
 * This will delete **both** permanent and temporary worlds if
 * you want to remove permanent worlds but delete temporary worlds
 * you want to call [removeCustomLevel] instead.
 *
 * The level's directory will be deleted after it is removed.
 *
 * @param level The level to delete.
 * @return `true` if the level was deleted, `false` otherwise.
 * @see removeCustomLevel
 */
public fun MinecraftServer.deleteCustomLevel(level: CustomLevel): Boolean {
    this.unloadCustomLevel(level, false)
    val directory = this.getDimensionPath(level.dimension())
    if (directory.isDirectory()) {
        try {
            PathUtils.deleteDirectory(directory)
            return true
        } catch (e: IOException) {
            ArcadeUtils.logger.warn("Failed to delete level directory", e)
            PathUtils.deleteOnExit(directory)
        }
    }
    return false
}

public fun ServerLevel.setCustomMobSpawningRules(rules: CustomMobSpawningRules?) {
    this.getExtension<LevelCustomMobSpawningExtension>().rules = rules
}

public fun MinecraftServer.getDimensionPath(dimension: ResourceKey<Level>): Path {
    return (this as MinecraftServerAccessor).storage.getDimensionPath(dimension)
}

private fun MinecraftServer.unloadCustomLevel(level: CustomLevel, save: Boolean): Boolean {
    if ((this as MinecraftServerAccessor).levels.remove(level.dimension(), level)) {
        LevelPersistenceTracker.unmark(level.dimension())

        level.onUnload()
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

private fun ServerLevel.removePlayers() {
    val players = this.players()
    if (players.isEmpty()) {
        return
    }

    val overworld = this.server.overworld()
    for (player in players.toList()) {
        val position = player.adjustSpawnLocation(overworld, overworld.sharedSpawnPos).bottomCenter
        player.teleportTo(Location.of(position.x, position.y, position.z, 0.0F, 0.0F, overworld))
    }

    if (players.isNotEmpty()) {
        ArcadeUtils.logger.warn("Failed to remove players from closing world, kicking players")
        for (player in players.toList()) {
            player.connection.disconnect(Component.literal("Failed to remove you before world closed"))
        }
    }
}